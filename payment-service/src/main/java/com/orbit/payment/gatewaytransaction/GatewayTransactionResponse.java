package com.orbit.payment.gatewaytransaction;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a GatewayTransaction. */
public record GatewayTransactionResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String gatewayRef,
        String requestId,
        String responseCode,
        int latencyMs) {

    static GatewayTransactionResponse from(GatewayTransaction item) {
        return new GatewayTransactionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getGatewayRef(),
                item.getRequestId(),
                item.getResponseCode(),
                item.getLatencyMs());
    }
}
