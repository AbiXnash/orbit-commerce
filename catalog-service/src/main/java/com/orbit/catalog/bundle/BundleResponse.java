package com.orbit.catalog.bundle;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Bundle. */
public record BundleResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String bundleType,
        int itemCount,
        BigDecimal discountAmount,
        boolean featured) {

    static BundleResponse from(Bundle item) {
        return new BundleResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getBundleType(),
                item.getItemCount(),
                item.getDiscountAmount(),
                item.isFeatured());
    }
}
