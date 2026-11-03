package com.orbit.notification.bouncedemail;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a BouncedEmail. */
public record BouncedEmailResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String messageRef,
        String email,
        String bounceType,
        Instant occurredAt) {

    static BouncedEmailResponse from(BouncedEmail item) {
        return new BouncedEmailResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getMessageRef(),
                item.getEmail(),
                item.getBounceType(),
                item.getOccurredAt());
    }
}
