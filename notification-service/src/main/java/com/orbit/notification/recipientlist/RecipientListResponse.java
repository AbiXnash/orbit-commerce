package com.orbit.notification.recipientlist;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a RecipientList. */
public record RecipientListResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String listName,
        int memberCount,
        boolean dynamic,
        Instant refreshedAt) {

    static RecipientListResponse from(RecipientList item) {
        return new RecipientListResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getListName(),
                item.getMemberCount(),
                item.isDynamic(),
                item.getRefreshedAt());
    }
}
