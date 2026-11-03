package com.orbit.notification.notificationpreference;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a NotificationPreference. */
public record NotificationPreferenceResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String channel,
        boolean enabled,
        String quietHours) {

    static NotificationPreferenceResponse from(NotificationPreference item) {
        return new NotificationPreferenceResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getChannel(),
                item.isEnabled(),
                item.getQuietHours());
    }
}
