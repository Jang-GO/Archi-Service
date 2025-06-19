package com.archiservice.recommend.dto.response;

import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRecommendResponseDto {

    List<PlanDetailResponseDto> plans;
    List<VasDetailResponseDto> vass;
    List<CouponDetailResponseDto> coupons;
    String description;

}
