package com.orbit.customer.preference;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Preference. */
public record PreferenceResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String channel) {

    static PreferenceResponse from(Preference item) {
        return new PreferenceResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getChannel());
    }
}
