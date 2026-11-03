package com.orbit.order.quote;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Quote. */
public record QuoteResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String validUntil) {

    static QuoteResponse from(Quote item) {
        return new QuoteResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getValidUntil());
    }
}
