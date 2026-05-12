package com.bitdance.growth.dto;

import java.time.LocalDate;

public record GoalDto(
    Long id,
    Long userId,
    String goalPeriod,
    Integer targetMinutes,
    Integer targetTimes,
    Integer currentMinutes,
    Integer currentTimes,
    LocalDate startDate,
    LocalDate endDate,
    String goalStatus
) {}
