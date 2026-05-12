package com.bitdance.growth.dto;

import java.time.OffsetDateTime;

public record CheckinDto(
    Long id,
    Long userId,
    Long danceStyleId,
    Long studioId,
    Long courseScheduleId,
    Long practicePostId,
    Integer durationMinutes,
    String feelingText,
    Boolean isPublic,
    OffsetDateTime checkinAt
) {}
