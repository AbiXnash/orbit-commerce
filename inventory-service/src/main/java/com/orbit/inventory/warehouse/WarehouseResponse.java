package com.orbit.inventory.warehouse;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Warehouse. */
public record WarehouseResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String city,
        String postalCode) {

    static WarehouseResponse from(Warehouse item) {
        return new WarehouseResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCity(),
                item.getPostalCode());
    }
}
