package com.bitdance.merchant.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubmitClaimRequest(
    Long studioId,
    @Pattern(regexp = "owner_claim|operator_claim|new_studio") String claimType,
    Long businessLicenseAssetId,
    @Size(max = 1000) String submittedRemark,
    String studioName,
    String brandName,
    Long cityId,
    Long businessDistrictId,
    String address,
    BigDecimal longitude,
    BigDecimal latitude,
    String contactPhone,
    String intro,
    String businessHours,
    Long coverAssetId
) {
    public SubmitClaimRequest(
        Long studioId,
        String claimType,
        Long businessLicenseAssetId,
        String submittedRemark
    ) {
        this(
            studioId,
            claimType,
            businessLicenseAssetId,
            submittedRemark,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
