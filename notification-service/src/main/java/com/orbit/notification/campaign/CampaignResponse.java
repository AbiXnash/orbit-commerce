package com.orbit.notification.campaign;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Campaign. */
public record CampaignResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String audience,
        String owner) {

    static CampaignResponse from(Campaign item) {
        return new CampaignResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getAudience(),
                item.getOwner());
    }
}
