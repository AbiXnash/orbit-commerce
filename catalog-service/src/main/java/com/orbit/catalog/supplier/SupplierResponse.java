package com.orbit.catalog.supplier;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Supplier. */
public record SupplierResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String contactEmail,
        String taxNumber) {

    static SupplierResponse from(Supplier item) {
        return new SupplierResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getContactEmail(),
                item.getTaxNumber());
    }
}
