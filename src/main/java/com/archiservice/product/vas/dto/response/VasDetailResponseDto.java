package com.archiservice.product.vas.dto.response;

import com.archiservice.product.vas.domain.Vas;
import com.archiservice.review.summary.dto.SimplifiedSummaryResult;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class VasDetailResponseDto {
    private Long vasId;
    private String vasName;
    private Integer price;
    private Integer discountedPrice;
    private Integer saleRate;
    private String imageUrl;
    private String vasDescription;
    private List<String> tags;
    private String category;
    private boolean isOnSale;

    private SimplifiedSummaryResult reviewSummary;

    public static VasDetailResponseDto from(Vas vas, List<String> tags, String category, SimplifiedSummaryResult simplifiedSummaryResult) {
        return VasDetailResponseDto.builder()
                .vasId(vas.getVasId())
                .vasName(vas.getVasName())
                .price(vas.getPrice())
                .discountedPrice(vas.getDiscountedPrice())
                .saleRate(vas.getSaleRate())
                .imageUrl(vas.getImageUrl())
                .vasDescription(vas.getVasDescription())
                .tags(tags)
                .category(category)
                .isOnSale(vas.isOnSale())
                .reviewSummary(simplifiedSummaryResult)
                .build();
    }

    public static VasDetailResponseDto from(Vas vas, List<String> tags, String category) {
        return VasDetailResponseDto.builder()
                .vasId(vas.getVasId())
                .vasName(vas.getVasName())
                .price(vas.getPrice())
                .discountedPrice(vas.getDiscountedPrice())
                .saleRate(vas.getSaleRate())
                .imageUrl(vas.getImageUrl())
                .vasDescription(vas.getVasDescription())
                .tags(tags)
                .category(category)
                .isOnSale(vas.isOnSale())
                .build();
    }
}
