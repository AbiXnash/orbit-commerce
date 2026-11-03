package com.orbit.inventory.backorder;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Backorder. */
public record BackorderResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String customerRef,
        int quantity,
        Instant promisedAt) {

    static BackorderResponse from(Backorder item) {
        return new BackorderResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getCustomerRef(),
                item.getQuantity(),
                item.getPromisedAt());
    }
}
