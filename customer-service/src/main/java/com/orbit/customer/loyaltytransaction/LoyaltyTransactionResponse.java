package com.orbit.customer.loyaltytransaction;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a LoyaltyTransaction. */
public record LoyaltyTransactionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String accountRef,
        String transactionType,
        int points,
        Instant occurredAt) {

    static LoyaltyTransactionResponse from(LoyaltyTransaction item) {
        return new LoyaltyTransactionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAccountRef(),
                item.getTransactionType(),
                item.getPoints(),
                item.getOccurredAt());
    }
}
