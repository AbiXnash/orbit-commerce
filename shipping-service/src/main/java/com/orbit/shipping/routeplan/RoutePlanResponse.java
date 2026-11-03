package com.orbit.shipping.routeplan;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a RoutePlan. */
public record RoutePlanResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String routeCode,
        int stopCount,
        Instant plannedFor,
        boolean optimized) {

    static RoutePlanResponse from(RoutePlan item) {
        return new RoutePlanResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRouteCode(),
                item.getStopCount(),
                item.getPlannedFor(),
                item.isOptimized());
    }
}
