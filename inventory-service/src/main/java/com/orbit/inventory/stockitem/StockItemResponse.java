package com.orbit.inventory.stockitem;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a StockItem. */
public record StockItemResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String sku,
        String binLocation) {

    static StockItemResponse from(StockItem item) {
        return new StockItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSku(),
                item.getBinLocation());
    }
}
