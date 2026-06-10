package com.bitdance.coachops.dto;

import java.time.OffsetDateTime;

public record CertificationDto(
    Long id,
    Long userId,
    String applicationType,
    String coachType,
    String applicationStatus,
    String remark,
    Long reviewedByUserId,
    OffsetDateTime reviewedAt,
    String reviewRemark,
    OffsetDateTime createdAt
) {
    public CertificationDto(
        Long id,
        Long userId,
        String applicationType,
        String applicationStatus,
        String remark,
        Long reviewedByUserId,
        OffsetDateTime reviewedAt,
        String reviewRemark,
        OffsetDateTime createdAt
    ) {
        this(id, userId, applicationType, "freelance", applicationStatus, remark, reviewedByUserId, reviewedAt, reviewRemark, createdAt);
    }
}
