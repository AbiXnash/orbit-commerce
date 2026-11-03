package com.orbit.payment.cardtoken;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CardToken. */
public record CardTokenResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String tokenRef,
        String last4,
        Instant expiresAt,
        boolean active) {

    static CardTokenResponse from(CardToken item) {
        return new CardTokenResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getTokenRef(),
                item.getLast4(),
                item.getExpiresAt(),
                item.getActive());
    }
}
