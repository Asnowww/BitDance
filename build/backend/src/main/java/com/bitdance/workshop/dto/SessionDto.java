package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SessionDto(
    Long id,
    Long workshopId,
    String sessionName,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    Integer capacity,
    BigDecimal priceAmount,
    Integer soldCount,
    Integer checkinCount,
    String sessionStatus
) {
    public SessionDto(
        Long id,
        Long workshopId,
        String sessionName,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Integer capacity,
        Integer soldCount,
        Integer checkinCount,
        String sessionStatus
    ) {
        this(id, workshopId, sessionName, startAt, endAt, capacity, null, soldCount, checkinCount, sessionStatus);
    }
}
