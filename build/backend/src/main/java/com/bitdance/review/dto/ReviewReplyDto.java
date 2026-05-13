package com.bitdance.review.dto;

import java.time.OffsetDateTime;

public record ReviewReplyDto(
    Long id,
    Long reviewId,
    Long replierUserId,
    String replyContent,
    Boolean isOfficial,
    OffsetDateTime createdAt
) {}
