package com.orbit.shipping.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the inventory service. */
@FeignClient(name = "inventory-service", path = "/api/warehouses")
public interface WarehouseClient {

    @GetMapping("/by-code/{code}")
    RemoteWarehouse getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a Warehouse this service needs. */
    record RemoteWarehouse(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
