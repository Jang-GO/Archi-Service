package com.archiservice.recommend.dto.response;

import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendCouponResponseDto {
    List<CouponDetailResponseDto> coupons;
}
