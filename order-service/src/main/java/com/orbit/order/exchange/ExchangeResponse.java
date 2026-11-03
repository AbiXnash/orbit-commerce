package com.orbit.order.exchange;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Exchange. */
public record ExchangeResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String originalSku,
        String replacementSku,
        boolean shipped) {

    static ExchangeResponse from(Exchange item) {
        return new ExchangeResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getOriginalSku(),
                item.getReplacementSku(),
                item.isShipped());
    }
}
