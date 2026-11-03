package com.orbit.order.orderline;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a OrderLine. */
public record OrderLineResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String sku) {

    static OrderLineResponse from(OrderLine item) {
        return new OrderLineResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getSku());
    }
}
