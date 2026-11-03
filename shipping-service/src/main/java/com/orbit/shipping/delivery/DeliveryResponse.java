package com.orbit.shipping.delivery;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Delivery. */
public record DeliveryResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String driver) {

    static DeliveryResponse from(Delivery item) {
        return new DeliveryResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getDriver());
    }
}
