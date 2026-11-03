package com.orbit.order.pricing;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class QuotePricing {

    private final OrderPricing pricing;

    public QuotePricing(OrderPricing pricing) {
        this.pricing = pricing;
    }

    public BigDecimal quote(BigDecimal net, BigDecimal tax) {
        return pricing.grossTotal(net, tax);
    }
}
