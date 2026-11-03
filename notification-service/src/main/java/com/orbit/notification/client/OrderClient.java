package com.orbit.notification.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the order service. */
@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderClient {

    @GetMapping("/by-code/{code}")
    RemoteOrder getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a Order this service needs. */
    record RemoteOrder(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
