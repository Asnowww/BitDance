package com.bitdance.workshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "workshop_order")
public class WorkshopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64, unique = true)
    private String orderNo;

    @Column(name = "workshop_id", nullable = false)
    private Long workshopId;

    @Column(name = "workshop_session_id", nullable = false)
    private Long workshopSessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount_payable", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPayable;

    @Column(name = "amount_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "order_status", nullable = false, length = 16)
    private String orderStatus = "pending_payment";

    @Column(name = "payment_txn_no", length = 128)
    private String paymentTxnNo;

    @Column(name = "refund_reason", columnDefinition = "text")
    private String refundReason;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String v) { this.orderNo = v; }
    public Long getWorkshopId() { return workshopId; }
    public void setWorkshopId(Long v) { this.workshopId = v; }
    public Long getWorkshopSessionId() { return workshopSessionId; }
    public void setWorkshopSessionId(Long v) { this.workshopSessionId = v; }
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
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String v) { this.refundReason = v; }
    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(OffsetDateTime v) { this.canceledAt = v; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime v) { this.paidAt = v; }
    public OffsetDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(OffsetDateTime v) { this.refundedAt = v; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime v) { this.completedAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
