package com.archiservice.product.coupon.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.dto.response.CouponResponseDto;

public interface CouponService {
    Page<CouponResponseDto> getAllCoupons(Pageable pageable);
    CouponDetailResponseDto getCouponDetail(Long couponId);
    CouponDetailResponseDto findCouponByName(String couponName);
}
