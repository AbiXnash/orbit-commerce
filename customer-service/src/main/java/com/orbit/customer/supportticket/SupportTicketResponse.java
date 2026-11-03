package com.orbit.customer.supportticket;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SupportTicket. */
public record SupportTicketResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String subject) {

    static SupportTicketResponse from(SupportTicket item) {
        return new SupportTicketResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getSubject());
    }
}
