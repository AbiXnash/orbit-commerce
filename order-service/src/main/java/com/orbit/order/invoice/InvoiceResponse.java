package com.orbit.order.invoice;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Invoice. */
public record InvoiceResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String taxNumber) {

    static InvoiceResponse from(Invoice item) {
        return new InvoiceResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getTaxNumber());
    }
}
