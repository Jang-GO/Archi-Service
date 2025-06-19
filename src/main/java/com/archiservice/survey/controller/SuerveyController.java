package com.archiservice.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.survey.dto.response.QuestionResponseDto;
import com.archiservice.survey.service.SurveyService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SuerveyController {

	private final SurveyService surveyService;
	
	@GetMapping("/questions")
	public ResponseEntity<ApiResponse<QuestionResponseDto>> getQuestion(
			@RequestParam(required = false, name = "nextQuestionId") Long nextQuestionId,
			@RequestParam(required = false, name = "tagCode") Long tagCode,
			@RequestParam(defaultValue = "false", name = "fromPrevious") boolean fromPrevious,
			HttpSession session){
		return ResponseEntity.ok(surveyService.getQuestion(nextQuestionId, tagCode, fromPrevious, session));
	}
	
	@PostMapping("/save")
	public ResponseEntity<ApiResponse<String>> saveResult(
			@AuthenticationPrincipal CustomUser customUser,
			HttpSession session){
		Long userId = customUser.getId();
		return ResponseEntity.ok(surveyService.saveResult(userId, session));
	}
	
	@GetMapping("/previous")
    public ApiResponse<QuestionResponseDto> previousQuestion(HttpSession session) {
        return surveyService.getPreviousQuestion(session);
    }
}

