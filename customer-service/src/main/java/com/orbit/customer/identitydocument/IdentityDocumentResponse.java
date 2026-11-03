package com.orbit.customer.identitydocument;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a IdentityDocument. */
public record IdentityDocumentResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String documentType,
        String documentNumber,
        boolean verified) {

    static IdentityDocumentResponse from(IdentityDocument item) {
        return new IdentityDocumentResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getDocumentType(),
                item.getDocumentNumber(),
                item.isVerified());
    }
}
