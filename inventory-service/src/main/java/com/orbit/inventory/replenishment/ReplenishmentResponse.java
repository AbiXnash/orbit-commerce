package com.orbit.inventory.replenishment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Replenishment. */
public record ReplenishmentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String supplierCode,
        String leadTime) {

    static ReplenishmentResponse from(Replenishment item) {
        return new ReplenishmentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSupplierCode(),
                item.getLeadTime());
    }
}
