package com.orbit.payment.payment;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Payment. */
public record PaymentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String method) {

    static PaymentResponse from(Payment item) {
        return new PaymentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getMethod());
    }
}
