package com.orbit.payment.refundline;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a RefundLine. */
public record RefundLineResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String refundRef,
        String paymentRef,
        String reason,
        boolean approved) {

    static RefundLineResponse from(RefundLine item) {
        return new RefundLineResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRefundRef(),
                item.getPaymentRef(),
                item.getReason(),
                item.isApproved());
    }
}
