package com.orbit.customer.customertag;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CustomerTag. */
public record CustomerTagResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String slug,
        String colorHex,
        String description,
        boolean featured) {

    static CustomerTagResponse from(CustomerTag item) {
        return new CustomerTagResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSlug(),
                item.getColorHex(),
                item.getDescription(),
                item.isFeatured());
    }
}
