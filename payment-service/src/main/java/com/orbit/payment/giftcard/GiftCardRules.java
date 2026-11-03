package com.orbit.payment.giftcard;

import java.math.BigDecimal;

/** Early draft, not used yet. */
public final class GiftCardRules {
    private GiftCardRules() {
    }

    public static boolean covers(BigDecimal balance, BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}
