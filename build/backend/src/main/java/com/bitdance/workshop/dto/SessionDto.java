package com.bitdance.workshop.dto;

import java.time.OffsetDateTime;

public record SessionDto(
    Long id,
    Long workshopId,
    String sessionName,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    Integer capacity,
    Integer soldCount,
    Integer checkinCount,
    String sessionStatus
) {}
