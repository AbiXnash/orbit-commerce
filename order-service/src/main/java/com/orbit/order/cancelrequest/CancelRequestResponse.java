package com.orbit.order.cancelrequest;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CancelRequest. */
public record CancelRequestResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String reason,
        String requestedBy,
        boolean approved) {

    static CancelRequestResponse from(CancelRequest item) {
        return new CancelRequestResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getReason(),
                item.getRequestedBy(),
                item.isApproved());
    }
}
