package com.orbit.payment.dispute;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Dispute. */
public record DisputeResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String paymentRef,
        String reason,
        Instant openedAt,
        boolean resolved) {

    static DisputeResponse from(Dispute item) {
        return new DisputeResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPaymentRef(),
                item.getReason(),
                item.getOpenedAt(),
                item.isResolved());
    }
}
