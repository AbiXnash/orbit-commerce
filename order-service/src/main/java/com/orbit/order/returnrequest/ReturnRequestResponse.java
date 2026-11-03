package com.orbit.order.returnrequest;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ReturnRequest. */
public record ReturnRequestResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderCode,
        String reason) {

    static ReturnRequestResponse from(ReturnRequest item) {
        return new ReturnRequestResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderCode(),
                item.getReason());
    }
}
