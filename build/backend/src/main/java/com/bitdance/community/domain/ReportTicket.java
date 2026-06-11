package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "report_ticket")
public class ReportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason_code", nullable = false, length = 32)
    private String reasonCode;

    @Column(name = "reason_detail", columnDefinition = "text")
    private String reasonDetail;

    @Column(name = "report_status", nullable = false, length = 16)
    private String reportStatus = "pending";

    @Column(name = "handled_by_user_id")
    private Long handledByUserId;

    @Column(name = "handled_at")
    private OffsetDateTime handledAt;

    @Column(name = "handle_result", columnDefinition = "text")
    private String handleResult;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long v) { this.reporterUserId = v; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String v) { this.targetType = v; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long v) { this.targetId = v; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String v) { this.reasonCode = v; }
    public String getReasonDetail() { return reasonDetail; }
    public void setReasonDetail(String v) { this.reasonDetail = v; }
    public String getReportStatus() { return reportStatus; }
    public void setReportStatus(String v) { this.reportStatus = v; }
    public Long getHandledByUserId() { return handledByUserId; }
    public void setHandledByUserId(Long v) { this.handledByUserId = v; }
    public OffsetDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(OffsetDateTime v) { this.handledAt = v; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String v) { this.handleResult = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
