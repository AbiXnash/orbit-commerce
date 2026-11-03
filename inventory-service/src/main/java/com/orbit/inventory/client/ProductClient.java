package com.orbit.inventory.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the catalog service. */
@FeignClient(name = "catalog-service", path = "/api/products")
public interface ProductClient {

    @GetMapping("/by-code/{code}")
    RemoteProduct getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a Product this service needs. */
    record RemoteProduct(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
