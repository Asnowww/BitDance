package com.bitdance.coachops.dto;

import java.time.OffsetDateTime;

public record CertificationDto(
    Long id,
    Long userId,
    String applicationType,
    String applicationStatus,
    String remark,
    Long reviewedByUserId,
    OffsetDateTime reviewedAt,
    String reviewRemark,
    OffsetDateTime createdAt
) {}
