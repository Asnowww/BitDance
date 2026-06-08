package com.bitdance.merchant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitClaimRequest(
    @NotNull Long studioId,
    @Pattern(regexp = "owner_claim|operator_claim", message = "claimType 必须是 owner_claim/operator_claim")
    String claimType,
    Long businessLicenseAssetId,
    @Size(max = 1000) String submittedRemark
) {}
