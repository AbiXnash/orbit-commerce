package com.orbit.inventory.reservation;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Reservation. */
public record ReservationResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String holdReason) {

    static ReservationResponse from(Reservation item) {
        return new ReservationResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getHoldReason());
    }
}
