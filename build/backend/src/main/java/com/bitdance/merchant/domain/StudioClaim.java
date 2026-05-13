package com.bitdance.merchant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "studio_claim")
public class StudioClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(name = "claim_type", nullable = false, length = 32)
    private String claimType = "owner_claim";

    @Column(name = "claim_status", nullable = false, length = 16)
    private String claimStatus = "pending";

    @Column(name = "business_license_asset_id")
    private Long businessLicenseAssetId;

    @Column(name = "submitted_remark", columnDefinition = "text")
    private String submittedRemark;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_remark", columnDefinition = "text")
    private String reviewRemark;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long v) { this.applicantUserId = v; }
    public String getClaimType() { return claimType; }
    public void setClaimType(String v) { this.claimType = v; }
    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String v) { this.claimStatus = v; }
    public Long getBusinessLicenseAssetId() { return businessLicenseAssetId; }
    public void setBusinessLicenseAssetId(Long v) { this.businessLicenseAssetId = v; }
    public String getSubmittedRemark() { return submittedRemark; }
    public void setSubmittedRemark(String v) { this.submittedRemark = v; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Long v) { this.reviewedByUserId = v; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime v) { this.reviewedAt = v; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String v) { this.reviewRemark = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
