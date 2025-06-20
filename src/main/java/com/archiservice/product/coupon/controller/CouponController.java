package com.archiservice.product.coupon.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.dto.response.CouponResponseDto;
import com.archiservice.product.coupon.service.CouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponseDto>>> getAllCoupons(@PageableDefault(size = 20, sort = "couponId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCoupons(pageable)));
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponseDto>> getCouponDetail(@PathVariable("couponId") Long couponId) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponDetail(couponId)));
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CouponDetailResponseDto>> searchCouponByName(@RequestParam("name") String couponName) {
       return ResponseEntity.ok(ApiResponse.success(couponService.findCouponByName(couponName)));
    }
}
