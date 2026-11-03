package com.orbit.shipping.carrieraccount;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a CarrierAccount. */
public record CarrierAccountResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String carrierCode,
        String accountNumber,
        boolean active,
        Instant openedAt) {

    static CarrierAccountResponse from(CarrierAccount item) {
        return new CarrierAccountResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCarrierCode(),
                item.getAccountNumber(),
                item.getActive(),
                item.getOpenedAt());
    }
}
