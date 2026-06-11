package com.bitdance.growth.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record GrowthReportDto(
    String period,
    LocalDate startDate,
    LocalDate endDate,
    long totalSessions,
    long totalMinutes,
    int activeDays,
    int styleCount,
    long workCount,
    long badgeCount,
    Integer goalTargetTimes,
    Integer goalCurrentTimes,
    Integer goalTargetMinutes,
    Integer goalCurrentMinutes,
    double goalProgress,
    Map<Long, Long> styleSessions,
    List<TimelineItem> highlights,
    String suggestion
) {}
