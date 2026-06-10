package com.bitdance.workshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateSessionRequest(
    @NotNull Long workshopId,
    @Size(max = 100) String sessionName,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @NotNull @Min(1) Integer capacity,
    BigDecimal priceAmount
) {
    public CreateSessionRequest(
        Long workshopId,
        String sessionName,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Integer capacity
    ) {
        this(workshopId, sessionName, startAt, endAt, capacity, null);
    }
}
