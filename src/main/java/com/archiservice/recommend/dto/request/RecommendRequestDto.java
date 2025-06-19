package com.archiservice.recommend.dto.request;

import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.recommend.dto.response.RecommendCouponResponseDto;
import com.archiservice.recommend.dto.response.RecommendPlanResponseDto;
import com.archiservice.recommend.dto.response.RecommendVasResponseDto;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RecommendRequestDto {

    List<PlanDetailResponseDto> plans;
    List<VasDetailResponseDto> vass;
    List<CouponDetailResponseDto> coupons;

    public static RecommendRequestDto from(RecommendPlanResponseDto planResponseDto ,
                                           RecommendVasResponseDto vassResponseDto,
                                           RecommendCouponResponseDto couponResponseDto) {
        return RecommendRequestDto.builder()
                .plans(planResponseDto.getPlans())
                .vass(vassResponseDto.getVass())
                .coupons(couponResponseDto.getCoupons())
                .build();

    }

}
