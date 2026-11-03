package com.orbit.shipping.packagingtype;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PackagingType. */
public record PackagingTypeResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String dimensions,
        boolean reusable,
        BigDecimal maxWeight) {

    static PackagingTypeResponse from(PackagingType item) {
        return new PackagingTypeResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getDimensions(),
                item.isReusable(),
                item.getMaxWeight());
    }
}
