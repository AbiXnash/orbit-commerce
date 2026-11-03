package com.orbit.order.giftcard;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a GiftCard. */
public record GiftCardResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String cardNumber,
        String currency,
        Instant expiresAt,
        boolean redeemed) {

    static GiftCardResponse from(GiftCard item) {
        return new GiftCardResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCardNumber(),
                item.getCurrency(),
                item.getExpiresAt(),
                item.isRedeemed());
    }
}
