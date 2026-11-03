package com.orbit.inventory.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps ProductClient, so a failing catalog service doesn't take this one down with it. */
@Component
public class ProductGateway {

    private static final Logger log = LoggerFactory.getLogger(ProductGateway.class);

    private final ProductClient client;

    public ProductGateway(ProductClient client) {
        this.client = client;
    }

    public Optional<ProductClient.RemoteProduct> find(String code) {
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
            log.warn("catalog service unavailable: {}", failure.getMessage());
            return 0;
        }
    }
}
