package com.archiservice.advertisement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponseDto {
    private long vasId;
    private String vasName;
    private String description;
}
