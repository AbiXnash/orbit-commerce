package com.orbit.catalog.productvariant;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ProductVariant. */
public record ProductVariantResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String skuSuffix,
        String optionName,
        String optionValue,
        String barcode) {

    static ProductVariantResponse from(ProductVariant item) {
        return new ProductVariantResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSkuSuffix(),
                item.getOptionName(),
                item.getOptionValue(),
                item.getBarcode());
    }
}
