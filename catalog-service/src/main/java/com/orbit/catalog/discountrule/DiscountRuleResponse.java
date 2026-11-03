package com.orbit.catalog.discountrule;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a DiscountRule. */
public record DiscountRuleResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String couponCode,
        BigDecimal percentage,
        int minQuantity,
        boolean stackable) {

    static DiscountRuleResponse from(DiscountRule item) {
        return new DiscountRuleResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCouponCode(),
                item.getPercentage(),
                item.getMinQuantity(),
                item.isStackable());
    }
}
