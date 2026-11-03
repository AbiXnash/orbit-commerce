package com.orbit.shipping.manifest;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Manifest. */
public record ManifestResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String carrierCode,
        String depot) {

    static ManifestResponse from(Manifest item) {
        return new ManifestResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCarrierCode(),
                item.getDepot());
    }
}
