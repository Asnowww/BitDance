package com.bitdance.courseorder.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CourseOrderDto(
    Long id,
    String orderNo,
    Long courseId,
    Long courseScheduleId,
    Long studioId,
    Long coachId,
    Long userId,
    BigDecimal amountPayable,
    BigDecimal amountPaid,
    String orderStatus,
    String paymentTxnNo,
    String checkinCode,
    OffsetDateTime paidAt,
    OffsetDateTime canceledAt,
    OffsetDateTime refundRequestedAt,
    OffsetDateTime refundedAt,
    OffsetDateTime completedAt,
    OffsetDateTime createdAt
) {}
