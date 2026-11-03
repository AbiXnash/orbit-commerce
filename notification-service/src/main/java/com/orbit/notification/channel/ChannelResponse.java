package com.orbit.notification.channel;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Channel. */
public record ChannelResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String channelType,
        String provider,
        boolean enabled,
        int rateLimit) {

    static ChannelResponse from(Channel item) {
        return new ChannelResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getChannelType(),
                item.getProvider(),
                item.isEnabled(),
                item.getRateLimit());
    }
}
