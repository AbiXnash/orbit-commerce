package com.orbit.shipping.customsdocument;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CustomsDocument. */
public record CustomsDocumentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String shipmentRef,
        String documentType,
        boolean cleared,
        BigDecimal declaredValue) {

    static CustomsDocumentResponse from(CustomsDocument item) {
        return new CustomsDocumentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getShipmentRef(),
                item.getDocumentType(),
                item.isCleared(),
                item.getDeclaredValue());
    }
}
