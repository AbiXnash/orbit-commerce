package com.orbit.inventory.safetystock;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SafetyStock. */
public record SafetyStockResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String productCode,
        int minimumLevel,
        int reorderPoint,
        int leadDays) {

    static SafetyStockResponse from(SafetyStock item) {
        return new SafetyStockResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getProductCode(),
                item.getMinimumLevel(),
                item.getReorderPoint(),
                item.getLeadDays());
    }
}
