package com.orbit.catalog.attribute;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Attribute. */
public record AttributeResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String unit,
        String dataType) {

    static AttributeResponse from(Attribute item) {
        return new AttributeResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getUnit(),
                item.getDataType());
    }
}
