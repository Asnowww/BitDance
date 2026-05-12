package com.bitdance.buddy.dto;

import java.time.OffsetDateTime;

public record RatingDto(
    Long id,
    Long practicePostId,
    Long fromUserId,
    Long toUserId,
    Short punctualityScore,
    Short friendlinessScore,
    Short skillMatchScore,
    String ratingComment,
    OffsetDateTime createdAt
) {}
