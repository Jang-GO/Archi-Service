package com.archiservice.user.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TendencyUpdateRequestDto {
	private List<String> tags;
    private Long tagCode;
}
