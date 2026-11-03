package com.orbit.payment.chargeback;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Chargeback. */
public record ChargebackResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String paymentCode,
        String network) {

    static ChargebackResponse from(Chargeback item) {
        return new ChargebackResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPaymentCode(),
                item.getNetwork());
    }
}
