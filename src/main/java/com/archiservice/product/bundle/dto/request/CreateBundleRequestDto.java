package com.archiservice.product.bundle.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreateBundleRequestDto {
    private Long planId;
    private Long vasId;
    private Long couponId;
    // 태그코드는 서비스로직에서 추출가능
}
