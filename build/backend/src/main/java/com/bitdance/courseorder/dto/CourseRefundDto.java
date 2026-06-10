package com.bitdance.courseorder.dto;

import java.time.OffsetDateTime;

public record CourseRefundDto(
    Long id,
    Long courseOrderId,
    Long requesterUserId,
    String refundReason,
    String requestStatus,
    Long reviewedByUserId,
    OffsetDateTime reviewedAt,
    String reviewRemark,
    OffsetDateTime createdAt
) {}
