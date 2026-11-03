package com.orbit.customer.contact;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Contact. */
public record ContactResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String email,
        String phone,
        boolean primaryContact) {

    static ContactResponse from(Contact item) {
        return new ContactResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getEmail(),
                item.getPhone(),
                item.isPrimaryContact());
    }
}
