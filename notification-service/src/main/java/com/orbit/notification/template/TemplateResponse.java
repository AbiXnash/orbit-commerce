package com.orbit.notification.template;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Template. */
public record TemplateResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String locale,
        String subject) {

    static TemplateResponse from(Template item) {
        return new TemplateResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getLocale(),
                item.getSubject());
    }
}
