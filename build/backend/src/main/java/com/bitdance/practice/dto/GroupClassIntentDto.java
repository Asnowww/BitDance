package com.bitdance.practice.dto;

import java.time.OffsetDateTime;

public record GroupClassIntentDto(
    Long id,
    Long creatorUserId,
    Long studioId,
    Long danceStyleId,
    String preferredTimeNote,
    Integer targetPeopleCount,
    Integer currentPeopleCount,
    String intentStatus,
    Boolean joinedByMe,
    OffsetDateTime createdAt
) {}
