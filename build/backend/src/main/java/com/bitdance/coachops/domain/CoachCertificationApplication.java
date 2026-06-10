package com.bitdance.coachops.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "coach_certification_application")
public class CoachCertificationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "application_type", nullable = false, length = 32)
    private String applicationType = "independent";

    @Column(name = "coach_type", nullable = false, length = 16)
    private String coachType = "freelance";

    @Column(name = "application_status", nullable = false, length = 16)
    private String applicationStatus = "pending";

    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_remark", columnDefinition = "text")
    private String reviewRemark;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getApplicationType() { return applicationType; }
    public void setApplicationType(String v) { this.applicationType = v; }
    public String getCoachType() { return coachType; }
    public void setCoachType(String v) { this.coachType = v; }
    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String v) { this.applicationStatus = v; }
    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Long v) { this.reviewedByUserId = v; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime v) { this.reviewedAt = v; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String v) { this.reviewRemark = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
