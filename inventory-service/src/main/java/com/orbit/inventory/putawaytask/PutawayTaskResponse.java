package com.orbit.inventory.putawaytask;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PutawayTask. */
public record PutawayTaskResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String inboundRef,
        String binCode,
        int quantity,
        boolean completed) {

    static PutawayTaskResponse from(PutawayTask item) {
        return new PutawayTaskResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getInboundRef(),
                item.getBinCode(),
                item.getQuantity(),
                item.isCompleted());
    }
}
