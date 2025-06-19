package com.archiservice.product.coupon.dto.response;

import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.review.summary.dto.SimplifiedSummaryResult;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDetailResponseDto {
    private Long couponId;
    private String couponName;
    private Integer price;
    private String imageUrl;
    private List<String> tags;
    private String category;

    private SimplifiedSummaryResult reviewSummary;

    public static CouponDetailResponseDto from(Coupon coupon, List<String> tags, String category, SimplifiedSummaryResult reviewSummary) {
        return CouponDetailResponseDto.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .price(coupon.getPrice())
                .imageUrl(coupon.getImageUrl())
                .tags(tags)
                .category(category)
                .reviewSummary(reviewSummary)
                .build();
    }

    public static CouponDetailResponseDto from(Coupon coupon, List<String> tags, String category) {
        return CouponDetailResponseDto.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .price(coupon.getPrice())
                .imageUrl(coupon.getImageUrl())
                .tags(tags)
                .category(category)
                .build();
    }
}
