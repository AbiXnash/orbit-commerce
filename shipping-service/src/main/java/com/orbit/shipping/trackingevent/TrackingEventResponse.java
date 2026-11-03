package com.orbit.shipping.trackingevent;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a TrackingEvent. */
public record TrackingEventResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String trackingNumber,
        String location,
        String eventCode,
        Instant occurredAt) {

    static TrackingEventResponse from(TrackingEvent item) {
        return new TrackingEventResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getTrackingNumber(),
                item.getLocation(),
                item.getEventCode(),
                item.getOccurredAt());
    }
}
