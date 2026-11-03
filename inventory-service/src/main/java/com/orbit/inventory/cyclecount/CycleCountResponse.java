package com.orbit.inventory.cyclecount;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CycleCount. */
public record CycleCountResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String zone,
        String countedBy) {

    static CycleCountResponse from(CycleCount item) {
        return new CycleCountResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getZone(),
                item.getCountedBy());
    }
}
