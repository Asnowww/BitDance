package com.bitdance.workshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workshop_checkin")
public class WorkshopCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workshop_order_id", nullable = false, unique = true)
    private Long workshopOrderId;

    @Column(name = "workshop_session_id", nullable = false)
    private Long workshopSessionId;

    @Column(name = "checked_in_by_user_id")
    private Long checkedInByUserId;

    @Column(name = "checkin_status", nullable = false, length = 16)
    private String checkinStatus = "checked_in";

    @Column(name = "checkin_code", length = 64)
    private String checkinCode;

    @Column(name = "checked_in_at", nullable = false)
    private OffsetDateTime checkedInAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getWorkshopOrderId() { return workshopOrderId; }
    public void setWorkshopOrderId(Long v) { this.workshopOrderId = v; }
    public Long getWorkshopSessionId() { return workshopSessionId; }
    public void setWorkshopSessionId(Long v) { this.workshopSessionId = v; }
    public Long getCheckedInByUserId() { return checkedInByUserId; }
    public void setCheckedInByUserId(Long v) { this.checkedInByUserId = v; }
    public String getCheckinStatus() { return checkinStatus; }
    public void setCheckinStatus(String v) { this.checkinStatus = v; }
    public String getCheckinCode() { return checkinCode; }
    public void setCheckinCode(String v) { this.checkinCode = v; }
    public OffsetDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(OffsetDateTime v) { this.checkedInAt = v; }
}
