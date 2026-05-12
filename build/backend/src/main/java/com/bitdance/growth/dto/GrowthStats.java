package com.bitdance.growth.dto;

import java.time.OffsetDateTime;

public record GrowthStats(
    long totalSessions,
    long totalMinutes,
    int totalDays,
    int styleCount,
    int streakDays,
    OffsetDateTime lastCheckinAt
) {}
