package com.archiservice.product.vas.domain;

import com.archiservice.common.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vass")
public class Vas extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vas_id")
    private Long vasId;

    @Column(name = "vas_name")
    private String vasName;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "vas_description")
    private String vasDescription;

    @Column(name = "sale_rate")
    private Integer saleRate;

    @Column(name = "tag_code")
    private Long tagCode;

    @Column(name = "category_code", length = 3)
    private String categoryCode;

    public Money getDiscountedPrice() {
        if (saleRate == null || saleRate == 0) {
            return price;
        }
        return price.applyDiscount(saleRate);
    }

    public boolean isOnSale() {
        return saleRate != null && saleRate > 0;
    }

    public Integer getPriceAsInteger() {
        return price.getAmount();
    }

    public Integer getDiscountedPriceAsInteger() {
        return getDiscountedPrice().getAmount();
    }
}
