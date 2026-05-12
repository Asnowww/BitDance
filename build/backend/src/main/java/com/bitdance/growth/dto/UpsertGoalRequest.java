package com.bitdance.growth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UpsertGoalRequest(
    @Pattern(regexp = "weekly|monthly") String goalPeriod,
    @Min(0) @Max(10000) Integer targetMinutes,
    @Min(0) @Max(500) Integer targetTimes,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
