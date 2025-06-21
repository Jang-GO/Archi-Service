package com.archiservice.advertisement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BannerRequestDto {
    private long vasId;
    private String mainTag;
    private String subTag;
    private String vasName;
    private String vasDescription;
}
