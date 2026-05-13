package com.bitdance.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StudioCoachRelationDto(
    Long id,
    Long studioId,
    Long coachId,
    String relationType,
    String relationStatus,
    String settlementMode,
    BigDecimal settlementRatio,
    Long invitedByUserId,
    Long approvedByUserId,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    OffsetDateTime createdAt
) {}
