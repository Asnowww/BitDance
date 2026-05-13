package com.bitdance.merchant.dto;

import java.time.OffsetDateTime;

public record StudioClaimDto(
    Long id,
    Long studioId,
    Long applicantUserId,
    String claimType,
    String claimStatus,
    Long businessLicenseAssetId,
    String submittedRemark,
    Long reviewedByUserId,
    OffsetDateTime reviewedAt,
    String reviewRemark,
    OffsetDateTime createdAt
) {}
