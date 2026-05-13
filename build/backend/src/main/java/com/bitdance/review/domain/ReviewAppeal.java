package com.bitdance.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "review_appeal")
public class ReviewAppeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "appellant_user_id", nullable = false)
    private Long appellantUserId;

    @Column(name = "appeal_reason", nullable = false, columnDefinition = "text")
    private String appealReason;

    @Column(name = "appeal_status", nullable = false, length = 16)
    private String appealStatus = "pending";

    @Column(name = "evidence_note", columnDefinition = "text")
    private String evidenceNote;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_remark", columnDefinition = "text")
    private String reviewRemark;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long v) { this.reviewId = v; }
    public Long getAppellantUserId() { return appellantUserId; }
    public void setAppellantUserId(Long v) { this.appellantUserId = v; }
    public String getAppealReason() { return appealReason; }
    public void setAppealReason(String v) { this.appealReason = v; }
    public String getAppealStatus() { return appealStatus; }
    public void setAppealStatus(String v) { this.appealStatus = v; }
    public String getEvidenceNote() { return evidenceNote; }
    public void setEvidenceNote(String v) { this.evidenceNote = v; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Long v) { this.reviewedByUserId = v; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime v) { this.reviewedAt = v; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String v) { this.reviewRemark = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
