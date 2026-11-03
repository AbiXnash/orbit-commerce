package com.orbit.customer.customer;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Customer. */
public record CustomerResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String email,
        String phone) {

    static CustomerResponse from(Customer item) {
        return new CustomerResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getEmail(),
                item.getPhone());
    }
}
