package com.archiservice.survey.service;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.survey.dto.response.QuestionResponseDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface SurveyService {
	ApiResponse<QuestionResponseDto> getQuestion(Long nextQuestionId, Long tagCode, boolean fromPrevious, HttpSession session);
	ApiResponse<String> saveResult(Long userId, HttpSession session);
	ApiResponse<QuestionResponseDto> getPreviousQuestion(HttpSession session);
}
