package com.orbit.payment.paymentmethod;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PaymentMethod. */
public record PaymentMethodResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String methodType,
        String last4,
        String holder,
        String token) {

    static PaymentMethodResponse from(PaymentMethod item) {
        return new PaymentMethodResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getMethodType(),
                item.getLast4(),
                item.getHolder(),
                item.getToken());
    }
}
