package com.orbit.notification.recipientlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/** What a client sends to create or update a RecipientList. */
public record CreateRecipientListRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 160) String name,
        @Min(0) int priority,
        BigDecimal amount,
        String listName,
        int memberCount,
        boolean dynamic,
        Instant refreshedAt) {
}
