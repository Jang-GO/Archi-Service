package com.archiservice.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.exception.BusinessException;
import com.archiservice.tendency.survey.domain.Option;
import com.archiservice.tendency.survey.domain.Question;
import com.archiservice.tendency.survey.dto.response.QuestionResponseDto;
import com.archiservice.tendency.survey.repository.QuestionRepository;
import com.archiservice.tendency.survey.service.impl.SurveyServiceImpl;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

class SurveyServiceTest {

	@Mock
	private QuestionRepository questionRepository;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private TagMetaService tagMetaService;
	
	@Mock
	private HttpSession session;
	
	@InjectMocks
	private SurveyServiceImpl surveyService;
	
	@BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
	
	@DisplayName("성향 테스트 시작")
	@Test
	void testGetQuestion_Start() {
		Question question = new Question();
	    question.setSurveyQuestionId(1L);
	    question.setQuestionText("1번 질문");
	    question.setOptions(List.of(new Option()));
	    
	    when(session.getAttribute("tagCodeSum")).thenReturn(null);
	    when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
		
//	    ApiResponse<QuestionResponseDto> response = surveyService.getQuestion(1L, 5L, session);
		
		verify(session).removeAttribute("tagCodeSum");
		verify(session).setAttribute("tagCodeSum", 5L);
//		assertEquals("1번 질문", response.getData().getQuestionText());
	}
	
	@DisplayName("성향 테스트 진행")
	@Test
	void testGetQuestion_Progress() {
		Question question = new Question();
		question.setSurveyQuestionId(2L);
		question.setQuestionText("2번 질문");
		question.setOptions(List.of(new Option()));
		
		when(session.getAttribute("tagCodeSum")).thenReturn(10L);
		when(questionRepository.findById(2L)).thenReturn(Optional.of(question));
		
//		ApiResponse<QuestionResponseDto> response = surveyService.getQuestion(2L, 3L, session);
		
		verify(session).setAttribute("tagCodeSum", 13L);
//		assertEquals("2번 질문", response.getData().getQuestionText());
	}
	
	@DisplayName("성향 테스트 종료")
	@Test
	void testGetQuestion_End() {
		when(session.getAttribute("tagCodeSum")).thenReturn(10L);
		when(tagMetaService.extractTagsFromCode(15L)).thenReturn(List.of("태그1", "태그2"));
		
//		ApiResponse<QuestionResponseDto> response = surveyService.getQuestion(null, 5L, session);
		
		verify(session).setAttribute("tagCodeSum", 15L);
		verify(session).setAttribute("tagCodes", List.of("태그1", "태그2"));
//		assertEquals("성향 테스트 종료", response.getData().getQuestionText());
	}

	@DisplayName("성향 테스트 결과 등록 성공")
	@Test
	void testSaveResult_Success() {
		User user = new User();
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(session.getAttribute("tagCodeSum")).thenReturn(100L);
		
//		ApiResponse<String> response = surveyService.saveResult(1L, session);
		
		verify(userRepository).save(user);
		assertEquals(100L, user.getTagCode());
		verify(session).removeAttribute("tagCodes");
//		assertEquals("성향 저장", response.getData());
		
	}
	
	@DisplayName("성향 테스트 결과 등록 실패")
	@Test
	void testSaveResult_NoTagCode() {
		User user = new User();
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(session.getAttribute("tagCodeSum")).thenReturn(null);
		
//		BusinessException e = assertThrows(BusinessException.class, () ->
//        surveyService.saveResult(1L, session));
//		assertEquals("저장된 태그코드가 없습니다.", e.getMessage());
		
	}
	
}





