package com.orbit.notification.pushsubscription;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PushSubscription. */
public record PushSubscriptionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String deviceToken,
        String platform,
        boolean active) {

    static PushSubscriptionResponse from(PushSubscription item) {
        return new PushSubscriptionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getDeviceToken(),
                item.getPlatform(),
                item.getActive());
    }
}
