package com.orbit.shipping.pickuppoint;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PickupPoint. */
public record PickupPointResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String city,
        String openingHours) {

    static PickupPointResponse from(PickupPoint item) {
        return new PickupPointResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCity(),
                item.getOpeningHours());
    }
}
