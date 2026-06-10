package com.bitdance.courseorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "course_order")
public class CourseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    @Column(name = "course_schedule_id", nullable = false)
    private Long courseScheduleId;
    @Column(name = "studio_id", nullable = false)
    private Long studioId;
    @Column(name = "coach_id")
    private Long coachId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "amount_payable", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPayable;
    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;
    @Column(name = "order_status", nullable = false, length = 24)
    private String orderStatus = "pending_payment";
    @Column(name = "payment_txn_no", length = 128)
    private String paymentTxnNo;
    @Column(name = "checkin_code", length = 8)
    private String checkinCode;
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;
    @Column(name = "refund_requested_at")
    private OffsetDateTime refundRequestedAt;
    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String v) { this.orderNo = v; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public Long getCourseScheduleId() { return courseScheduleId; }
    public void setCourseScheduleId(Long v) { this.courseScheduleId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getCoachId() { return coachId; }
    public void setCoachId(Long v) { this.coachId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public BigDecimal getAmountPayable() { return amountPayable; }
    public void setAmountPayable(BigDecimal v) { this.amountPayable = v; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal v) { this.amountPaid = v; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String v) { this.orderStatus = v; }
    public String getPaymentTxnNo() { return paymentTxnNo; }
    public void setPaymentTxnNo(String v) { this.paymentTxnNo = v; }
    public String getCheckinCode() { return checkinCode; }
    public void setCheckinCode(String v) { this.checkinCode = v; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime v) { this.paidAt = v; }
    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(OffsetDateTime v) { this.canceledAt = v; }
    public OffsetDateTime getRefundRequestedAt() { return refundRequestedAt; }
    public void setRefundRequestedAt(OffsetDateTime v) { this.refundRequestedAt = v; }
    public OffsetDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(OffsetDateTime v) { this.refundedAt = v; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime v) { this.completedAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
