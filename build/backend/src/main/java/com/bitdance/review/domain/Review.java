package com.bitdance.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "overall_score", precision = 4, scale = 2, nullable = false)
    private BigDecimal overallScore;

    @Column(name = "content_text", columnDefinition = "text")
    private String contentText;

    @Column(name = "verified_source_type", length = 32)
    private String verifiedSourceType;

    @Column(name = "verified_source_ref_id")
    private Long verifiedSourceRefId;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = Boolean.FALSE;

    @Column(name = "weight_factor", precision = 6, scale = 3, nullable = false)
    private BigDecimal weightFactor = new BigDecimal("1.000");

    @Column(name = "review_status", nullable = false, length = 16)
    private String reviewStatus = "published";

    @Column(name = "risk_level", nullable = false)
    private Short riskLevel = 0;

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = Boolean.FALSE;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String v) { this.targetType = v; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long v) { this.targetId = v; }
    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal v) { this.overallScore = v; }
    public String getContentText() { return contentText; }
    public void setContentText(String v) { this.contentText = v; }
    public String getVerifiedSourceType() { return verifiedSourceType; }
    public void setVerifiedSourceType(String v) { this.verifiedSourceType = v; }
    public Long getVerifiedSourceRefId() { return verifiedSourceRefId; }
    public void setVerifiedSourceRefId(Long v) { this.verifiedSourceRefId = v; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean v) { this.isVerified = v; }
    public BigDecimal getWeightFactor() { return weightFactor; }
    public void setWeightFactor(BigDecimal v) { this.weightFactor = v; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String v) { this.reviewStatus = v; }
    public Short getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Short v) { this.riskLevel = v; }
    public Integer getHelpfulCount() { return helpfulCount; }
    public Boolean getIsPinned() { return isPinned; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime v) { this.publishedAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
