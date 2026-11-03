package com.orbit.order.cart;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Cart. */
public record CartResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String couponCode) {

    static CartResponse from(Cart item) {
        return new CartResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getCouponCode());
    }
}
