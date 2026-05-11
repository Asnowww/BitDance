package com.bitdance.practice.dto;

import java.time.OffsetDateTime;

public record JoinRequestDto(
    Long id,
    Long practicePostId,
    Long applicantUserId,
    String joinStatus,
    String joinMessage,
    Long actedByUserId,
    OffsetDateTime actedAt,
    OffsetDateTime createdAt
) {}
