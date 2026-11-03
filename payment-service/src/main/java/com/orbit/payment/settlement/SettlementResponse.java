package com.orbit.payment.settlement;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Settlement. */
public record SettlementResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String batchCode,
        String bank) {

    static SettlementResponse from(Settlement item) {
        return new SettlementResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getBatchCode(),
                item.getBank());
    }
}
