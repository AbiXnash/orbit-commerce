package com.orbit.catalog.category;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Category. */
public record CategoryResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String slug,
        String parentCode) {

    static CategoryResponse from(Category item) {
        return new CategoryResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSlug(),
                item.getParentCode());
    }
}
