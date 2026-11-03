package com.orbit.customer.communicationpreference;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CommunicationPreference. */
public record CommunicationPreferenceResponse(
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
        boolean optedIn,
        Instant lastUpdatedAt) {

    static CommunicationPreferenceResponse from(CommunicationPreference item) {
        return new CommunicationPreferenceResponse(
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
                item.isOptedIn(),
                item.getLastUpdatedAt());
    }
}
