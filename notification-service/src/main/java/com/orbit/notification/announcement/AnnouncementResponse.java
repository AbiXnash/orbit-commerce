package com.orbit.notification.announcement;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Announcement. */
public record AnnouncementResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String title,
        String audience,
        String body,
        boolean published) {

    static AnnouncementResponse from(Announcement item) {
        return new AnnouncementResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getTitle(),
                item.getAudience(),
                item.getBody(),
                item.isPublished());
    }
}
