package com.orbit.notification.subscriber;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Subscriber. */
public record SubscriberResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String email,
        String locale) {

    static SubscriberResponse from(Subscriber item) {
        return new SubscriberResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getEmail(),
                item.getLocale());
    }
}
