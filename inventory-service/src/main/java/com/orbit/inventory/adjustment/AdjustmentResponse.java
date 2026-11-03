package com.orbit.inventory.adjustment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Adjustment. */
public record AdjustmentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String reason,
        String approvedBy) {

    static AdjustmentResponse from(Adjustment item) {
        return new AdjustmentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getReason(),
                item.getApprovedBy());
    }
}
