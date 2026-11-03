package com.orbit.customer.feedback;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a Feedback. */
public record FeedbackResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        String comment,
        int rating,
        Instant submittedAt) {

    static FeedbackResponse from(Feedback item) {
        return new FeedbackResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCustomerRef(),
                item.getComment(),
                item.getRating(),
                item.getSubmittedAt());
    }
}
