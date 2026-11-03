package com.orbit.catalog.warranty;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Warranty. */
public record WarrantyResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String provider,
        int durationMonths,
        String termsUrl,
        boolean registrationRequired) {

    static WarrantyResponse from(Warranty item) {
        return new WarrantyResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getProvider(),
                item.getDurationMonths(),
                item.getTermsUrl(),
                item.isRegistrationRequired());
    }
}
