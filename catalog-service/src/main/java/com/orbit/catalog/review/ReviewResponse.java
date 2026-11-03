package com.orbit.catalog.review;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Review. */
public record ReviewResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String author,
        String headline) {

    static ReviewResponse from(Review item) {
        return new ReviewResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAuthor(),
                item.getHeadline());
    }
}
