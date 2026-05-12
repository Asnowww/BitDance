package com.bitdance.growth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateCheckinRequest(
    Long danceStyleId,
    Long studioId,
    Long courseScheduleId,
    Long practicePostId,
    @NotNull @Min(1) @Max(1440) Integer durationMinutes,
    @Size(max = 2000) String feelingText,
    Boolean isPublic,
    OffsetDateTime checkinAt
) {}
