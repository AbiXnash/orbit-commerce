package com.orbit.notification.newsletter;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Newsletter. */
public record NewsletterResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String subject,
        String audience,
        Instant scheduledAt,
        boolean sent) {

    static NewsletterResponse from(Newsletter item) {
        return new NewsletterResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSubject(),
                item.getAudience(),
                item.getScheduledAt(),
                item.isSent());
    }
}
