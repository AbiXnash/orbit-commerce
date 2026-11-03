package com.orbit.catalog.pricelist;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PriceList. */
public record PriceListResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String currency,
        String region) {

    static PriceListResponse from(PriceList item) {
        return new PriceListResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCurrency(),
                item.getRegion());
    }
}
