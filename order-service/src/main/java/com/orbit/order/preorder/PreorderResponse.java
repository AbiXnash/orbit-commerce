package com.orbit.order.preorder;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Preorder. */
public record PreorderResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String productCode,
        Instant releaseDate,
        BigDecimal deposit) {

    static PreorderResponse from(Preorder item) {
        return new PreorderResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getProductCode(),
                item.getReleaseDate(),
                item.getDeposit());
    }
}
