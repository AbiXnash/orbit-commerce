package com.orbit.payment.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the customer service. */
@FeignClient(name = "customer-service", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/by-code/{code}")
    RemoteCustomer getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a Customer this service needs. */
    record RemoteCustomer(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
