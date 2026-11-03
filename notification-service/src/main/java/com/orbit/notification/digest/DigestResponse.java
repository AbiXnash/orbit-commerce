package com.orbit.notification.digest;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Digest. */
public record DigestResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String recipient,
        String frequency) {

    static DigestResponse from(Digest item) {
        return new DigestResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRecipient(),
                item.getFrequency());
    }
}
