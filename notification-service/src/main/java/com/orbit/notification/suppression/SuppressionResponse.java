package com.orbit.notification.suppression;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Suppression. */
public record SuppressionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String email,
        String reason,
        Instant expiresAt,
        boolean active) {

    static SuppressionResponse from(Suppression item) {
        return new SuppressionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getEmail(),
                item.getReason(),
                item.getExpiresAt(),
                item.getActive());
    }
}
