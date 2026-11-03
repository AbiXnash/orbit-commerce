package com.orbit.catalog.suppliercontract;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SupplierContract. */
public record SupplierContractResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String contractRef,
        int renewalNoticeDays,
        boolean signed,
        Instant signedAt) {

    static SupplierContractResponse from(SupplierContract item) {
        return new SupplierContractResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getContractRef(),
                item.getRenewalNoticeDays(),
                item.isSigned(),
                item.getSignedAt());
    }
}
