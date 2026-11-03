package com.orbit.shipping.dispatch;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Dispatch. */
public record DispatchResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String routeRef,
        String driverName,
        String vehicleRef,
        boolean departed) {

    static DispatchResponse from(Dispatch item) {
        return new DispatchResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRouteRef(),
                item.getDriverName(),
                item.getVehicleRef(),
                item.isDeparted());
    }
}
