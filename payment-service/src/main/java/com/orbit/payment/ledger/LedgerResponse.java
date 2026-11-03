package com.orbit.payment.ledger;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Ledger. */
public record LedgerResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String account,
        String currency) {

    static LedgerResponse from(Ledger item) {
        return new LedgerResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAccount(),
                item.getCurrency());
    }
}
