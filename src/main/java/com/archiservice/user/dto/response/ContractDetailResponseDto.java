package com.archiservice.user.dto.response;

import com.archiservice.product.vas.domain.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailResponseDto {
    private String paymentMethod;
    private long contractPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String planName;
    private int planPrice;
    private String planCategory;
    private Integer monthData;
    private String callUsage;
    private String messageUsage;

    private String vasName;
    private Money vasPrice;
    private String vasCategory;
    private String vasDescription;
    private Integer saleRate;

    private String couponName;
    private Integer couponPrice;
    private String couponCategory;
}
