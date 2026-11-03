package com.orbit.shipping.route;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Route. */
public record RouteResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String origin,
        String destination) {

    static RouteResponse from(Route item) {
        return new RouteResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrigin(),
                item.getDestination());
    }
}
