package com.orbit.shipping.shippinglabel;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ShippingLabel. */
public record ShippingLabelResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String shipmentRef,
        String labelUrl,
        String format,
        boolean printed) {

    static ShippingLabelResponse from(ShippingLabel item) {
        return new ShippingLabelResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getShipmentRef(),
                item.getLabelUrl(),
                item.getFormat(),
                item.isPrinted());
    }
}
