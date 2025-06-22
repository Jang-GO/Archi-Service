package com.archiservice.product.vas.domain;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public class Money {
    private BigDecimal amount;

    public static final Money ZERO = new Money(0);

    protected Money() {}

    public Money(int amount) {
        this.amount = new BigDecimal(amount);
    }

    public Money(BigDecimal amount) {
        this.amount = amount.setScale(0, RoundingMode.HALF_UP);
    }

    public Money applyDiscount(int discountRate) {
        if (discountRate <= 0) return this;

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                new BigDecimal(discountRate).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal discounted = amount.multiply(discountMultiplier);

        // 10원 단위로 반올림
        BigDecimal rounded = discounted.divide(new BigDecimal(10), 0, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(10));

        return new Money(rounded);
    }

    public int getAmount() {
        return amount.intValue();
    }
}
