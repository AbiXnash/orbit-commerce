package com.orbit.shipping.pickuppoint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** What a client sends to create or update a PickupPoint. */
public record CreatePickupPointRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String city,
        String openingHours) {
}
