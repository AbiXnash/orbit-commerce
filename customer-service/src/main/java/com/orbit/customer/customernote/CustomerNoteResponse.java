package com.orbit.customer.customernote;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CustomerNote. */
public record CustomerNoteResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String author,
        String body,
        boolean pinned) {

    static CustomerNoteResponse from(CustomerNote item) {
        return new CustomerNoteResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getAuthor(),
                item.getBody(),
                item.isPinned());
    }
}
