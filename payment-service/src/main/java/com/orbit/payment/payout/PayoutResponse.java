package com.orbit.payment.payout;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Payout. */
public record PayoutResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String merchant,
        String iban) {

    static PayoutResponse from(Payout item) {
        return new PayoutResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getMerchant(),
                item.getIban());
    }
}
