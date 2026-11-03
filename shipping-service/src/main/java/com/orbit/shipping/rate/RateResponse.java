package com.orbit.shipping.rate;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Rate. */
public record RateResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String zone,
        String serviceLevel) {

    static RateResponse from(Rate item) {
        return new RateResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getZone(),
                item.getServiceLevel());
    }
}
