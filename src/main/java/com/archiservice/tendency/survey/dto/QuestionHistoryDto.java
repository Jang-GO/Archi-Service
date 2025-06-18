package com.archiservice.tendency.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionHistoryDto {
    private Long questionId;
    private Long tagCode;
}

