package com.orbit.payment.currencyrate;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CurrencyRate. */
public record CurrencyRateResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String baseCurrency,
        String quoteCurrency,
        BigDecimal rate,
        Instant quotedAt) {

    static CurrencyRateResponse from(CurrencyRate item) {
        return new CurrencyRateResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getBaseCurrency(),
                item.getQuoteCurrency(),
                item.getRate(),
                item.getQuotedAt());
    }
}
