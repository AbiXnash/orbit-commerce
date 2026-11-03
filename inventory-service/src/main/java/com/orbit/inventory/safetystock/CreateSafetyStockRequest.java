package com.orbit.inventory.safetystock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/** What a client sends to create or update a SafetyStock. */
public record CreateSafetyStockRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String productCode,
        int minimumLevel,
        int reorderPoint,
        int leadDays) {
}
