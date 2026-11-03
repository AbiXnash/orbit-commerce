package com.orbit.order.layaway;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Layaway. */
public record LayawayResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String orderRef,
        int installmentCount,
        BigDecimal paidAmount) {

    static LayawayResponse from(Layaway item) {
        return new LayawayResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getOrderRef(),
                item.getInstallmentCount(),
                item.getPaidAmount());
    }
}
