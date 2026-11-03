package com.orbit.payment.refund;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Refund. */
public record RefundResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String paymentCode,
        String reason) {

    static RefundResponse from(Refund item) {
        return new RefundResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPaymentCode(),
                item.getReason());
    }
}
