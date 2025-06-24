package com.archiservice.product.vas.dto.response;

import com.archiservice.product.vas.domain.Vas;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VasResponseDto {
    private Long vasId;
    private String vasName;
    private Integer price;
    private Integer discountedPrice;
    private Integer saleRate;
    private String imageUrl;
    private List<String> tags;
    private String category;

    public static VasResponseDto from(Vas vas, List<String> tags, String category) {
        return VasResponseDto.builder()
                .vasId(vas.getVasId())
                .vasName(vas.getVasName())
                .price(vas.getPriceAsInteger())
                .discountedPrice(vas.getDiscountedPriceAsInteger())
                .saleRate(vas.getSaleRate())
                .imageUrl(vas.getImageUrl())
                .tags(tags)
                .category(category)
                .build();
    }
}

