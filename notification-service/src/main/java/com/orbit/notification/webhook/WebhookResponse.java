package com.orbit.notification.webhook;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Webhook. */
public record WebhookResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String url,
        String eventType) {

    static WebhookResponse from(Webhook item) {
        return new WebhookResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getUrl(),
                item.getEventType());
    }
}
