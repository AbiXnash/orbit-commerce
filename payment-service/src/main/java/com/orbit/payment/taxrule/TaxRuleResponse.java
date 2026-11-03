package com.orbit.payment.taxrule;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a TaxRule. */
public record TaxRuleResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String region,
        String category) {

    static TaxRuleResponse from(TaxRule item) {
        return new TaxRuleResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getRegion(),
                item.getCategory());
    }
}
