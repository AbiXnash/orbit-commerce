package com.orbit.shipping.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps OrderClient, so a failing order service doesn't take this one down with it. */
@Component
public class OrderGateway {

    private static final Logger log = LoggerFactory.getLogger(OrderGateway.class);

    private final OrderClient client;

    public OrderGateway(OrderClient client) {
        this.client = client;
    }

    public Optional<OrderClient.RemoteOrder> find(String code) {
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
            log.warn("order service unavailable: {}", failure.getMessage());
            return 0;
        }
    }
}
