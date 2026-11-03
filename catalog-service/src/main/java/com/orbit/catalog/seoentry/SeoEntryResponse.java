package com.orbit.catalog.seoentry;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SeoEntry. */
public record SeoEntryResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String slug,
        String metaTitle,
        String metaDescription,
        String keywords) {

    static SeoEntryResponse from(SeoEntry item) {
        return new SeoEntryResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSlug(),
                item.getMetaTitle(),
                item.getMetaDescription(),
                item.getKeywords());
    }
}
