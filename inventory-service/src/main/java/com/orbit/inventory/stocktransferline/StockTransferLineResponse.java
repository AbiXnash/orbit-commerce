package com.orbit.inventory.stocktransferline;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a StockTransferLine. */
public record StockTransferLineResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String transferRef,
        String productCode,
        int quantity,
        int receivedQuantity) {

    static StockTransferLineResponse from(StockTransferLine item) {
        return new StockTransferLineResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getTransferRef(),
                item.getProductCode(),
                item.getQuantity(),
                item.getReceivedQuantity());
    }
}
