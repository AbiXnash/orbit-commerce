package com.orbit.notification.deliveryreport;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a DeliveryReport. */
public record DeliveryReportResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String messageCode,
        String provider) {

    static DeliveryReportResponse from(DeliveryReport item) {
        return new DeliveryReportResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getMessageCode(),
                item.getProvider());
    }
}
