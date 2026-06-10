package com.bitdance.courseorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "course_refund_request")
public class CourseRefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "course_order_id", nullable = false)
    private Long courseOrderId;
    @Column(name = "requester_user_id", nullable = false)
    private Long requesterUserId;
    @Column(name = "refund_reason", columnDefinition = "text")
    private String refundReason;
    @Column(name = "request_status", nullable = false, length = 24)
    private String requestStatus = "pending";
    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;
    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;
    @Column(name = "review_remark", columnDefinition = "text")
    private String reviewRemark;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getCourseOrderId() { return courseOrderId; }
    public void setCourseOrderId(Long v) { this.courseOrderId = v; }
    public Long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(Long v) { this.requesterUserId = v; }
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String v) { this.refundReason = v; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String v) { this.requestStatus = v; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Long v) { this.reviewedByUserId = v; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime v) { this.reviewedAt = v; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String v) { this.reviewRemark = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
