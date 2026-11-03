package com.orbit.order.subscription;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Subscription. */
public record SubscriptionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String plan) {

    static SubscriptionResponse from(Subscription item) {
        return new SubscriptionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getPlan());
    }
}
