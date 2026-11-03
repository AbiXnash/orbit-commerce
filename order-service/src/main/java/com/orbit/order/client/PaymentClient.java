package com.orbit.order.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the payment service. */
@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @GetMapping("/by-code/{code}")
    RemotePayment getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a Payment this service needs. */
    record RemotePayment(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
