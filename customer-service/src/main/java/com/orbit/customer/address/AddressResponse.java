package com.orbit.customer.address;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Address. */
public record AddressResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String city) {

    static AddressResponse from(Address item) {
        return new AddressResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getCity());
    }
}
