package com.orbit.shipping.vehicle;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Vehicle. */
public record VehicleResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String plate,
        String depot) {

    static VehicleResponse from(Vehicle item) {
        return new VehicleResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPlate(),
                item.getDepot());
    }
}
