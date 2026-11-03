package com.orbit.notification.sendattempt;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SendAttempt. */
public record SendAttemptResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String messageRef,
        String providerRef,
        int attemptNumber,
        boolean delivered) {

    static SendAttemptResponse from(SendAttempt item) {
        return new SendAttemptResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getMessageRef(),
                item.getProviderRef(),
                item.getAttemptNumber(),
                item.isDelivered());
    }
}
