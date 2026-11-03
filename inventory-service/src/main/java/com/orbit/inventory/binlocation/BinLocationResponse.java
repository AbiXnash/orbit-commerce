package com.orbit.inventory.binlocation;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a BinLocation. */
public record BinLocationResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String aisle,
        String rack,
        String shelf,
        int capacity) {

    static BinLocationResponse from(BinLocation item) {
        return new BinLocationResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAisle(),
                item.getRack(),
                item.getShelf(),
                item.getCapacity());
    }
}
