package com.archiservice.advertisement.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BannerRequestDto {
    private long vasId;
    private String mainTag;
    private String subTag;
    private String vasName;
    private String vasDescription;
}
