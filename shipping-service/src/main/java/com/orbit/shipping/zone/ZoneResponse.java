package com.orbit.shipping.zone;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Zone. */
public record ZoneResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String zoneCode,
        String region,
        String postalRange,
        BigDecimal surcharge) {

    static ZoneResponse from(Zone item) {
        return new ZoneResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getZoneCode(),
                item.getRegion(),
                item.getPostalRange(),
                item.getSurcharge());
    }
}
