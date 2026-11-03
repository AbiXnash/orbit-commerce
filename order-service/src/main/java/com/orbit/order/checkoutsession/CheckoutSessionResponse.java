package com.orbit.order.checkoutsession;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CheckoutSession. */
public record CheckoutSessionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String cartRef,
        Instant expiresAt,
        boolean completed) {

    static CheckoutSessionResponse from(CheckoutSession item) {
        return new CheckoutSessionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getCartRef(),
                item.getExpiresAt(),
                item.isCompleted());
    }
}
