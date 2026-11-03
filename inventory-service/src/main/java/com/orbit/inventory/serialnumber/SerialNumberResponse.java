package com.orbit.inventory.serialnumber;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SerialNumber. */
public record SerialNumberResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String serial,
        String productCode,
        Instant warrantyEndsAt,
        boolean assigned) {

    static SerialNumberResponse from(SerialNumber item) {
        return new SerialNumberResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSerial(),
                item.getProductCode(),
                item.getWarrantyEndsAt(),
                item.isAssigned());
    }
}
