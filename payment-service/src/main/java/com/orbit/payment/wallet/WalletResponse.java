package com.orbit.payment.wallet;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Wallet. */
public record WalletResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerCode,
        String currency) {

    static WalletResponse from(Wallet item) {
        return new WalletResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerCode(),
                item.getCurrency());
    }
}
