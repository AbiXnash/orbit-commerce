package com.orbit.shipping.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps WarehouseClient, so a failing inventory service doesn't take this one down with it. */
@Component
public class WarehouseGateway {

    private static final Logger log = LoggerFactory.getLogger(WarehouseGateway.class);

    private final WarehouseClient client;

    public WarehouseGateway(WarehouseClient client) {
        this.client = client;
    }

    public Optional<WarehouseClient.RemoteWarehouse> find(String code) {
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
