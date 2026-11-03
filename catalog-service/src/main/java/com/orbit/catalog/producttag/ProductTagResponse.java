package com.orbit.catalog.producttag;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ProductTag. */
public record ProductTagResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String slug,
        String description,
        String colorHex,
        boolean featured) {

    static ProductTagResponse from(ProductTag item) {
        return new ProductTagResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSlug(),
                item.getDescription(),
                item.getColorHex(),
                item.isFeatured());
    }
}
