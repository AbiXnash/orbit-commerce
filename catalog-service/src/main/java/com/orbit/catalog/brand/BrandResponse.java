package com.orbit.catalog.brand;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Brand. */
public record BrandResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String website,
        String country) {

    static BrandResponse from(Brand item) {
        return new BrandResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getWebsite(),
                item.getCountry());
    }
}
