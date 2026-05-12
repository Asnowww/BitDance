package com.bitdance.buddy.dto;

import java.time.OffsetDateTime;

public record BuddyDto(
    Long relationId,
    Long peerUserId,
    Long sourcePracticePostId,
    String relationStatus,
    OffsetDateTime createdAt
) {}
