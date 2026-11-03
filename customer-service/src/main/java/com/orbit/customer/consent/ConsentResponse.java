package com.orbit.customer.consent;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Consent. */
public record ConsentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String purpose) {

    static ConsentResponse from(Consent item) {
        return new ConsentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getPurpose());
    }
}
