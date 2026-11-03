package com.orbit.customer.loyaltyaccount;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a LoyaltyAccount. */
public record LoyaltyAccountResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String tier) {

    static LoyaltyAccountResponse from(LoyaltyAccount item) {
        return new LoyaltyAccountResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getTier());
    }
}
