package com.archiservice.survey.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.jwt.JwtUtil;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.survey.domain.Question;
import com.archiservice.survey.dto.QuestionHistoryDto;
import com.archiservice.survey.dto.response.QuestionResponseDto;
import com.archiservice.survey.repository.QuestionRepository;
import com.archiservice.survey.service.SurveyService;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService{

	private final QuestionRepository questionRepository;
	private final UserRepository userRepository;
	private final TagMetaService metaService;
	private final JwtUtil jwtUtil;
	
	@Override
	public ApiResponse<QuestionResponseDto> getQuestion(Long nextQuestionId, Long tagCode, boolean fromPrevious, HttpSession session) {
		
		if (Long.valueOf(1L).equals(nextQuestionId)) {
		    session.removeAttribute("tagCodeSum");
		    session.removeAttribute("questionHistory");
		}
		
	    Long tagCodeSum = (Long) session.getAttribute("tagCodeSum");

	    if (tagCodeSum == null) tagCodeSum = 0L;
	    if (tagCode != null) tagCodeSum += tagCode;
		
	    List<QuestionHistoryDto> history = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
	    if (history == null) {
	        history = new ArrayList<>();
	    }
	    if (fromPrevious && nextQuestionId != null) {
	        history.add(new QuestionHistoryDto(nextQuestionId, tagCode));
	    }
	    session.setAttribute("questionHistory", history);
	    session.setAttribute("tagCodeSum", tagCodeSum);
	    
		if (nextQuestionId == null) {
			// 성향 테스트 종료 지점
			List<String> tagCodes = metaService.extractTagsFromCode(tagCodeSum);
			return ApiResponse.success(new QuestionResponseDto("성향 테스트 종료", 0, List.of(), tagCodes));
		}
		
		Question question = questionRepository.findById(nextQuestionId)
					.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "문항이 존재하지 않습니다."));
		
		QuestionResponseDto questionResponseDto = QuestionResponseDto.from(question);
		
		return ApiResponse.success(questionResponseDto);
	}

	
	@Override
	public ApiResponse<String> saveResult(Long userId, HttpSession session) {
		User user = userRepository.findById(userId)
		        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		
		Long tagCode = (Long) session.getAttribute("tagCodeSum"); // List
		
		if (tagCode == null) {
		    throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "저장된 태그코드가 없습니다.");
		}

		user.setTagCode(tagCode);
		userRepository.save(user);
		
		// JWT tagCode 정보 삽입
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		claims.put("tagCode", tagCode);
		String tagCodeAccessToken = jwtUtil.generateCustomToken(claims, user.getEmail());
		
		session.removeAttribute("tagCodeSum");
		session.removeAttribute("questionHistory");
		return ApiResponse.success(tagCodeAccessToken);
	}

	@Override
	public ApiResponse<QuestionResponseDto> getPreviousQuestion(HttpSession session) {
	    List<QuestionHistoryDto> history = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
	    System.out.println("%%%%%%%%%%%%%%%%%" + history.size() + "%%%%%%%%%%%%%%%%%");
	    if (history == null || history.size() < 2) {
	        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이전 질문이 없습니다.");
	    }

	    // 현재 질문 제거
	    history.remove(history.size() - 1);
	    QuestionHistoryDto previous = history.get(history.size() - 1);
	    session.setAttribute("questionHistory", history);

	    // tagCodeSum 롤백
	    Long tagCodeSum = (Long) session.getAttribute("tagCodeSum");
	    if (tagCodeSum != null && previous.getTagCode() != null) {
	        session.setAttribute("tagCodeSum", tagCodeSum - previous.getTagCode());
	    }

	    System.out.println("%%%%%%%%%%%%%%%%%" + history.size() + "%%%%%%%%%%%%%%%%%");
	    return getQuestion(previous.getQuestionId(), null, true, session);
	}
	
}
