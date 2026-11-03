package com.orbit.catalog.productimage;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a ProductImage. */
public record ProductImageResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String url,
        String altText,
        String contentType,
        int position) {

    static ProductImageResponse from(ProductImage item) {
        return new ProductImageResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getUrl(),
                item.getAltText(),
                item.getContentType(),
                item.getPosition());
    }
}
