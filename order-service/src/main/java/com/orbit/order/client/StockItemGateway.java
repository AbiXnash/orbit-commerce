package com.orbit.order.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps StockItemClient, so a failing inventory service doesn't take this one down with it. */
@Component
public class StockItemGateway {

    private static final Logger log = LoggerFactory.getLogger(StockItemGateway.class);

    private final StockItemClient client;

    public StockItemGateway(StockItemClient client) {
        this.client = client;
    }

    public Optional<StockItemClient.RemoteStockItem> find(String code) {
        try {
            return Optional.of(client.getByCode(code));
        } catch (FeignException.NotFound missing) {
            return Optional.empty();
        }
    }

    public boolean isActive(String code) {
        return find(code).map(found -> "ACTIVE".equals(found.status())).orElse(false);
    }

    public long countActive() {
        try {
            return client.countActive();
        } catch (FeignException failure) {
            log.warn("inventory service unavailable: {}", failure.getMessage());
            return 0;
        }
    }
}
