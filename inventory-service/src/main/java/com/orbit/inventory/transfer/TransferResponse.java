package com.orbit.inventory.transfer;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Transfer. */
public record TransferResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String fromSite,
        String toSite) {

    static TransferResponse from(Transfer item) {
        return new TransferResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getFromSite(),
                item.getToSite());
    }
}
