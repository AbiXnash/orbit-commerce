package com.orbit.order.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Calls the inventory service. */
@FeignClient(name = "inventory-service", path = "/api/stock-items")
public interface StockItemClient {

    @GetMapping("/by-code/{code}")
    RemoteStockItem getByCode(@PathVariable("code") String code);

    @GetMapping("/stats/active")
    long countActive();

    /** The part of a StockItem this service needs. */
    record RemoteStockItem(Long id, String code, String name, String status, BigDecimal amount) {
    }
}
