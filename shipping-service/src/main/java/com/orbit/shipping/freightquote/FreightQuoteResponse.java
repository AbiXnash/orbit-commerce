package com.orbit.shipping.freightquote;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a FreightQuote. */
public record FreightQuoteResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String origin,
        String destination,
        Instant expiresAt,
        boolean accepted) {

    static FreightQuoteResponse from(FreightQuote item) {
        return new FreightQuoteResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrigin(),
                item.getDestination(),
                item.getExpiresAt(),
                item.isAccepted());
    }
}
