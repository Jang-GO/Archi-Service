package com.archiservice.product.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.archiservice.product.coupon.domain.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	Optional<Coupon> findByCouponName(String couponName);
}

