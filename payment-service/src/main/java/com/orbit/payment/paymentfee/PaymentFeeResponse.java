package com.orbit.payment.paymentfee;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PaymentFee. */
public record PaymentFeeResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String paymentRef,
        String feeType,
        boolean waived,
        BigDecimal feeAmount) {

    static PaymentFeeResponse from(PaymentFee item) {
        return new PaymentFeeResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPaymentRef(),
                item.getFeeType(),
                item.isWaived(),
                item.getFeeAmount());
    }
}
