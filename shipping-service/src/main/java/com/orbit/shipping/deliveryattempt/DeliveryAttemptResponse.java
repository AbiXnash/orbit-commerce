package com.orbit.shipping.deliveryattempt;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a DeliveryAttempt. */
public record DeliveryAttemptResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String deliveryRef,
        String outcome,
        int attemptNumber,
        Instant attemptedAt) {

    static DeliveryAttemptResponse from(DeliveryAttempt item) {
        return new DeliveryAttemptResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getDeliveryRef(),
                item.getOutcome(),
                item.getAttemptNumber(),
                item.getAttemptedAt());
    }
}
