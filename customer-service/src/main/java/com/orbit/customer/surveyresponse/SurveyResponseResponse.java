package com.orbit.customer.surveyresponse;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a SurveyResponse. */
public record SurveyResponseResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String surveyRef,
        String customerRef,
        String comment,
        Instant submittedAt) {

    static SurveyResponseResponse from(SurveyResponse item) {
        return new SurveyResponseResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getSurveyRef(),
                item.getCustomerRef(),
                item.getComment(),
                item.getSubmittedAt());
    }
}
