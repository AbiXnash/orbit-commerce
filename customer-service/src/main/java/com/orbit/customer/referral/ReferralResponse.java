package com.orbit.customer.referral;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Referral. */
public record ReferralResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String referrerRef,
        String refereeRef,
        String referralCode,
        boolean rewarded) {

    static ReferralResponse from(Referral item) {
        return new ReferralResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getReferrerRef(),
                item.getRefereeRef(),
                item.getReferralCode(),
                item.isRewarded());
    }
}
