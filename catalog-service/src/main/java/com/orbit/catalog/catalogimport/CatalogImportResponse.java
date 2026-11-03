package com.orbit.catalog.catalogimport;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CatalogImport. */
public record CatalogImportResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String source,
        String fileName,
        long rowCount,
        String checksum) {

    static CatalogImportResponse from(CatalogImport item) {
        return new CatalogImportResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSource(),
                item.getFileName(),
                item.getRowCount(),
                item.getChecksum());
    }
}
