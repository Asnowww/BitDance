package com.bitdance.booking.dto;

import java.time.OffsetDateTime;

public record BookingDto(
    Long id,
    Long userId,
    Long courseId,
    Long courseScheduleId,
    Long studioId,
    String bookingStatus,
    String contactPhone,
    String bookingNote,
    OffsetDateTime confirmedAt,
    OffsetDateTime attendedAt,
    OffsetDateTime canceledAt,
    String cancelReason,
    OffsetDateTime createdAt
) {}
