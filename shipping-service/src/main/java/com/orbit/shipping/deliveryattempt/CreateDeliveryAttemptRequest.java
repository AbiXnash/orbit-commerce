package com.orbit.shipping.deliveryattempt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/** What a client sends to create or update a DeliveryAttempt. */
public record CreateDeliveryAttemptRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String deliveryRef,
        String outcome,
        int attemptNumber,
        Instant attemptedAt) {
}
