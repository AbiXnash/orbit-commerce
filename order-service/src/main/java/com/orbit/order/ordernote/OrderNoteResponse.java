package com.orbit.order.ordernote;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a OrderNote. */
public record OrderNoteResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String author,
        String body,
        boolean pinned) {

    static OrderNoteResponse from(OrderNote item) {
        return new OrderNoteResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getAuthor(),
                item.getBody(),
                item.isPinned());
    }
}
