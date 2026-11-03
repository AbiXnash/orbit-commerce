package com.orbit.order.pricing;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderPricing {

    public BigDecimal grossTotal(BigDecimal net, BigDecimal tax) {
        return net.add(tax);
    }
}
