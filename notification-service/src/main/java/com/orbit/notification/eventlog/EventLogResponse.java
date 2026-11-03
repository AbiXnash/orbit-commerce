package com.orbit.notification.eventlog;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a EventLog. */
public record EventLogResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String eventType,
        String source,
        String payload,
        Instant occurredAt) {

    static EventLogResponse from(EventLog item) {
        return new EventLogResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getEventType(),
                item.getSource(),
                item.getPayload(),
                item.getOccurredAt());
    }
}
