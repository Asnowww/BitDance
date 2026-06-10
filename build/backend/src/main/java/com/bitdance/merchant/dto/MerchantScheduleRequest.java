package com.bitdance.merchant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record MerchantScheduleRequest(
    @NotNull Long courseId,
    @NotNull Long studioId,
    Long coachId,
    String classroomName,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Positive Integer capacity
) {}
