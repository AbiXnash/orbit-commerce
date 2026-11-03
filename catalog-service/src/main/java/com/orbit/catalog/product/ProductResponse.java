package com.orbit.catalog.product;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Product. */
public record ProductResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String sku,
        String brand) {

    static ProductResponse from(Product item) {
        return new ProductResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSku(),
                item.getBrand());
    }
}
