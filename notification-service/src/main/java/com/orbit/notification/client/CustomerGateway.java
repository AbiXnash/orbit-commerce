package com.orbit.notification.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps CustomerClient, so a failing customer service doesn't take this one down with it. */
@Component
public class CustomerGateway {

    private static final Logger log = LoggerFactory.getLogger(CustomerGateway.class);

    private final CustomerClient client;

    public CustomerGateway(CustomerClient client) {
        this.client = client;
    }

    public Optional<CustomerClient.RemoteCustomer> find(String code) {
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
            log.warn("customer service unavailable: {}", failure.getMessage());
            return 0;
        }
    }
}
