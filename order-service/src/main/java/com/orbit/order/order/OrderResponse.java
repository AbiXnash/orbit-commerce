package com.orbit.order.order;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Order. */
public record OrderResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String channel) {

    static OrderResponse from(Order item) {
        return new OrderResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getChannel());
    }
}
