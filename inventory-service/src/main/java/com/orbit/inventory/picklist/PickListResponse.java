package com.orbit.inventory.picklist;

import java.math.BigDecimal;
import java.time.Instant;

/** What a client sees of a PickList. */
public record PickListResponse(
        Long id,
        String code,
        String name,
        String status,
        int priority,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt,
        String orderRef,
        String pickerName,
        int lineCount,
        boolean picked) {

    static PickListResponse from(PickList item) {
        return new PickListResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getStatus(),
                item.getPriority(),
                item.getAmount(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getOrderRef(),
                item.getPickerName(),
                item.getLineCount(),
                item.isPicked());
    }
}
