package com.bitdance.catalog.dto;

import java.time.OffsetDateTime;

public record ScheduleItem(
    Long id,
    Long courseId,
    Long studioId,
    Long coachId,
    String classroomName,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    Integer capacity,
    Integer bookedCount,
    String status
) {}
