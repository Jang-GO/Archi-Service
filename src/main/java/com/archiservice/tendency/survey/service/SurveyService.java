package com.archiservice.tendency.survey.service;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.tendency.survey.dto.response.QuestionResponseDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface SurveyService {
	ApiResponse<QuestionResponseDto> getQuestion(Long nextQuestionId, Long tagCode, boolean fromPrevious, HttpSession session);
	ApiResponse<String> saveResult(Long userId, HttpSession session, HttpServletResponse response);
	ApiResponse<QuestionResponseDto> getPreviousQuestion(HttpSession session);
}
