package com.bitdance.courseorder.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCourseOrderRequest(
    @NotNull Long courseId,
    @NotNull Long courseScheduleId
) {}
