package com.orbit.catalog.pricebook;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PriceBook. */
public record PriceBookResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String currency,
        String region,
        String channel,
        Instant effectiveFrom) {

    static PriceBookResponse from(PriceBook item) {
        return new PriceBookResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCurrency(),
                item.getRegion(),
                item.getChannel(),
                item.getEffectiveFrom());
    }
}
