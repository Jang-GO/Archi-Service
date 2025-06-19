package com.archiservice.user.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TendencyUpdateRequestDto {
	private List<String> tagCodes;
    private Long tagCode;
}
