package com.orbit.inventory.inventoryaudit;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a InventoryAudit. */
public record InventoryAuditResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String auditor,
        Instant auditDate,
        String notes,
        BigDecimal variance) {

    static InventoryAuditResponse from(InventoryAudit item) {
        return new InventoryAuditResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAuditor(),
                item.getAuditDate(),
                item.getNotes(),
                item.getVariance());
    }
}
