package com.orbit.customer.segment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Segment. */
public record SegmentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String rule,
        String owner) {

    static SegmentResponse from(Segment item) {
        return new SegmentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRule(),
                item.getOwner());
    }
}
