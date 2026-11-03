package com.orbit.order.shipmenttracking;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ShipmentTracking. */
public record ShipmentTrackingResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String trackingNumber,
        String carrier,
        Instant lastEventAt) {

    static ShipmentTrackingResponse from(ShipmentTracking item) {
        return new ShipmentTrackingResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getTrackingNumber(),
                item.getCarrier(),
                item.getLastEventAt());
    }
}
