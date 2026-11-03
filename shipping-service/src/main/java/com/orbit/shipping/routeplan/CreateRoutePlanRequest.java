package com.orbit.shipping.routeplan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/** What a client sends to create or update a RoutePlan. */
public record CreateRoutePlanRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String routeCode,
        int stopCount,
        Instant plannedFor,
        boolean optimized) {
}
