package com.orbit.payment.payoutbatch;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PayoutBatch. */
public record PayoutBatchResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String batchRef,
        int payoutCount,
        boolean settled,
        Instant settledAt) {

    static PayoutBatchResponse from(PayoutBatch item) {
        return new PayoutBatchResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getBatchRef(),
                item.getPayoutCount(),
                item.isSettled(),
                item.getSettledAt());
    }
}
