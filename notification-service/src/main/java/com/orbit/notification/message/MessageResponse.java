package com.orbit.notification.message;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Message. */
public record MessageResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String recipient,
        String channel) {

    static MessageResponse from(Message item) {
        return new MessageResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRecipient(),
                item.getChannel());
    }
}
