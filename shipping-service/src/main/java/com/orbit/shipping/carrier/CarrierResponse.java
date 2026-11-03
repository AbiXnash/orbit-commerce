package com.orbit.shipping.carrier;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Carrier. */
public record CarrierResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String website,
        String serviceLevel) {

    static CarrierResponse from(Carrier item) {
        return new CarrierResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getWebsite(),
                item.getServiceLevel());
    }
}
