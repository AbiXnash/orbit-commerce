package com.orbit.payment.payoutbatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/** What a client sends to create or update a PayoutBatch. */
public record CreatePayoutBatchRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String batchRef,
        int payoutCount,
        boolean settled,
        Instant settledAt) {
}
