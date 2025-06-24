package com.archiservice.recommend.dto.response;

import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class RecommendResponseDto {

    List<PlanDetailResponseDto> plans;
    List<VasDetailResponseDto> vass;
    List<CouponDetailResponseDto> coupons;

    public static RecommendResponseDto from(RecommendPlanResponseDto planResponseDto ,
                                            RecommendVasResponseDto vassResponseDto,
                                            RecommendCouponResponseDto couponResponseDto) {
        return RecommendResponseDto.builder()
                .plans(planResponseDto.getPlans())
                .vass(vassResponseDto.getVass())
                .coupons(couponResponseDto.getCoupons())
                .build();

    }

}
