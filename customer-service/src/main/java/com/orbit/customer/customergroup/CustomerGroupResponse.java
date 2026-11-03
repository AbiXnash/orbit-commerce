package com.orbit.customer.customergroup;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CustomerGroup. */
public record CustomerGroupResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String slug,
        String description,
        int memberCount,
        boolean featured) {

    static CustomerGroupResponse from(CustomerGroup item) {
        return new CustomerGroupResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSlug(),
                item.getDescription(),
                item.getMemberCount(),
                item.isFeatured());
    }
}
