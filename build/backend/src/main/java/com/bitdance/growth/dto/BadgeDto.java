package com.bitdance.growth.dto;

import java.time.OffsetDateTime;

public record BadgeDto(
    Long id,
    Long badgeId,
    String sourceType,
    Long sourceRefId,
    OffsetDateTime awardedAt
) {}
