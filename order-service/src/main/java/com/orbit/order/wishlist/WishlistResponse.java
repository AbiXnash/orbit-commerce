package com.orbit.order.wishlist;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Wishlist. */
public record WishlistResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        int itemCount,
        boolean shared,
        String shareToken) {

    static WishlistResponse from(Wishlist item) {
        return new WishlistResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getItemCount(),
                item.isShared(),
                item.getShareToken());
    }
}
