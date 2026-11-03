package com.orbit.order.client;

import feign.FeignException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Wraps PaymentClient, so a failing payment service doesn't take this one down with it. */
@Component
public class PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymentGateway.class);

    private final PaymentClient client;

    public PaymentGateway(PaymentClient client) {
        this.client = client;
    }

    public Optional<PaymentClient.RemotePayment> find(String code) {
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
            log.warn("payment service unavailable: {}", failure.getMessage());
            return 0;
        }
    }
}
