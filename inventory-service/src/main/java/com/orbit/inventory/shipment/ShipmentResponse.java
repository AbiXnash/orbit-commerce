package com.orbit.inventory.shipment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Shipment. */
public record ShipmentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String carrier,
        String trackingCode) {

    static ShipmentResponse from(Shipment item) {
        return new ShipmentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCarrier(),
                item.getTrackingCode());
    }
}
