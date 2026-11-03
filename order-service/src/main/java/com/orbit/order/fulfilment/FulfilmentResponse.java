package com.orbit.order.fulfilment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Fulfilment. */
public record FulfilmentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String warehouse) {

    static FulfilmentResponse from(Fulfilment item) {
        return new FulfilmentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getWarehouse());
    }
}
