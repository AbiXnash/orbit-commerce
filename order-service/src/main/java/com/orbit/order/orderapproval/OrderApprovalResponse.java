package com.orbit.order.orderapproval;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a OrderApproval. */
public record OrderApprovalResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String approver,
        Instant decidedAt,
        boolean approved) {

    static OrderApprovalResponse from(OrderApproval item) {
        return new OrderApprovalResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getApprover(),
                item.getDecidedAt(),
                item.isApproved());
    }
}
