package com.orbit.catalog.collection;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Collection. */
public record CollectionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String season,
        String theme) {

    static CollectionResponse from(Collection item) {
        return new CollectionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSeason(),
                item.getTheme());
    }
}
