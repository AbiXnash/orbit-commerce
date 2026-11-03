package com.orbit.catalog.promotion;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Promotion. */
public record PromotionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String couponCode,
        String channel) {

    static PromotionResponse from(Promotion item) {
        return new PromotionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCouponCode(),
                item.getChannel());
    }
}
