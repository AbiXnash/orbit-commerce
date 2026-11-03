package com.orbit.inventory.lot;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Lot. */
public record LotResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String lotNumber,
        String supplierCode,
        Instant expiryDate,
        boolean quarantine) {

    static LotResponse from(Lot item) {
        return new LotResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getLotNumber(),
                item.getSupplierCode(),
                item.getExpiryDate(),
                item.isQuarantine());
    }
}
