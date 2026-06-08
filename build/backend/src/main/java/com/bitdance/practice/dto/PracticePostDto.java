package com.bitdance.practice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PracticePostDto(
    Long id,
    Long creatorUserId,
    Long danceStyleId,
    Long studioId,
    Long cityId,
    String locationName,
    String locationAddress,
    BigDecimal longitude,
    BigDecimal latitude,
    String skillLevel,
    Integer expectedPeopleMin,
    Integer expectedPeopleMax,
    Integer currentPeopleCount,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    OffsetDateTime expiresAt,
    String postStatus,
    String description,
    OffsetDateTime createdAt,
    Long distanceMeters
) {}
