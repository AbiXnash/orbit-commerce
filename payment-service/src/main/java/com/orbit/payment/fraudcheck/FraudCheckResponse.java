package com.orbit.payment.fraudcheck;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a FraudCheck. */
public record FraudCheckResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String paymentRef,
        String decision,
        boolean reviewed,
        int score) {

    static FraudCheckResponse from(FraudCheck item) {
        return new FraudCheckResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getPaymentRef(),
                item.getDecision(),
                item.isReviewed(),
                item.getScore());
    }
}
