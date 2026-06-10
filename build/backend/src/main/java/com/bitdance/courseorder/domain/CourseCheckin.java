package com.bitdance.courseorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "course_checkin")
public class CourseCheckin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "course_order_id", nullable = false, unique = true)
    private Long courseOrderId;
    @Column(name = "course_schedule_id", nullable = false)
    private Long courseScheduleId;
    @Column(name = "checkin_code", nullable = false, length = 8)
    private String checkinCode;
    @Column(name = "checked_in_by_user_id")
    private Long checkedInByUserId;
    @Column(name = "checked_in_at")
    private OffsetDateTime checkedInAt;
    @Column(name = "checkin_status", nullable = false, length = 24)
    private String checkinStatus = "active";
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getCourseOrderId() { return courseOrderId; }
    public void setCourseOrderId(Long v) { this.courseOrderId = v; }
    public Long getCourseScheduleId() { return courseScheduleId; }
    public void setCourseScheduleId(Long v) { this.courseScheduleId = v; }
    public String getCheckinCode() { return checkinCode; }
    public void setCheckinCode(String v) { this.checkinCode = v; }
    public Long getCheckedInByUserId() { return checkedInByUserId; }
    public void setCheckedInByUserId(Long v) { this.checkedInByUserId = v; }
    public OffsetDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(OffsetDateTime v) { this.checkedInAt = v; }
    public String getCheckinStatus() { return checkinStatus; }
    public void setCheckinStatus(String v) { this.checkinStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
