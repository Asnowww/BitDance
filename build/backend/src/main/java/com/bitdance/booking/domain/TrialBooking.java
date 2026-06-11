package com.bitdance.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "trial_booking")
public class TrialBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_schedule_id")
    private Long courseScheduleId;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "booking_status", nullable = false, length = 16)
    private String bookingStatus = "pending";

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "booking_note", columnDefinition = "text")
    private String bookingNote;

    @Column(name = "confirmed_by_user_id")
    private Long confirmedByUserId;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "attended_at")
    private OffsetDateTime attendedAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "cancel_reason", columnDefinition = "text")
    private String cancelReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public Long getCourseScheduleId() { return courseScheduleId; }
    public void setCourseScheduleId(Long v) { this.courseScheduleId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String v) { this.bookingStatus = v; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String v) { this.contactPhone = v; }
    public String getBookingNote() { return bookingNote; }
    public void setBookingNote(String v) { this.bookingNote = v; }
    public Long getConfirmedByUserId() { return confirmedByUserId; }
    public void setConfirmedByUserId(Long v) { this.confirmedByUserId = v; }
    public OffsetDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(OffsetDateTime v) { this.confirmedAt = v; }
    public OffsetDateTime getAttendedAt() { return attendedAt; }
    public void setAttendedAt(OffsetDateTime v) { this.attendedAt = v; }
    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(OffsetDateTime v) { this.canceledAt = v; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String v) { this.cancelReason = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
