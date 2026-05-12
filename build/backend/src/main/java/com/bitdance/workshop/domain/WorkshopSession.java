package com.bitdance.workshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workshop_session")
public class WorkshopSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workshop_id", nullable = false)
    private Long workshopId;

    @Column(name = "session_name", length = 100)
    private String sessionName;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @Column(name = "checkin_count", nullable = false)
    private Integer checkinCount = 0;

    @Column(name = "session_status", nullable = false, length = 16)
    private String sessionStatus = "scheduled";

    public Long getId() { return id; }
    public Long getWorkshopId() { return workshopId; }
    public String getSessionName() { return sessionName; }
    public OffsetDateTime getStartAt() { return startAt; }
    public OffsetDateTime getEndAt() { return endAt; }
    public Integer getCapacity() { return capacity; }
    public Integer getSoldCount() { return soldCount; }
    public Integer getCheckinCount() { return checkinCount; }
    public String getSessionStatus() { return sessionStatus; }
}
