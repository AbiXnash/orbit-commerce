package com.orbit.inventory.stockalert;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a StockAlert. */
public record StockAlertResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        int threshold,
        String channel,
        Instant lastTriggeredAt,
        boolean acknowledged) {

    static StockAlertResponse from(StockAlert item) {
        return new StockAlertResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getThreshold(),
                item.getChannel(),
                item.getLastTriggeredAt(),
                item.isAcknowledged());
    }
}
