package com.archiservice.tendency.survey.dto.response;

import com.archiservice.tendency.survey.domain.Option;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OptionResponseDto {
	String optionText;
	Long tagCode;
	Long nextQustionId;
	
	public static OptionResponseDto from(Option option) {
		return OptionResponseDto.builder()
				.optionText(option.getOptionText())
				.tagCode(option.getTagCode())
				.nextQustionId(option.getNextQuestionId())
				.build();
	}
}
