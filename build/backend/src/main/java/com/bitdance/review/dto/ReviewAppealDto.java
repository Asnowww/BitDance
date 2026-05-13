package com.bitdance.review.dto;

import java.time.OffsetDateTime;

public record ReviewAppealDto(
    Long id,
    Long reviewId,
    Long appellantUserId,
    String appealReason,
    String appealStatus,
    String evidenceNote,
    Long reviewedByUserId,
    OffsetDateTime reviewedAt,
    String reviewRemark,
    OffsetDateTime createdAt
) {}
