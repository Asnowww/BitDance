package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "course_schedule")
public class CourseSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "coach_id")
    private Long coachId;

    @Column(name = "classroom_name", length = 100)
    private String classroomName;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "booked_count", nullable = false)
    private Integer bookedCount = 0;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "scheduled";

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Long getStudioId() { return studioId; }
    public Long getCoachId() { return coachId; }
    public String getClassroomName() { return classroomName; }
    public OffsetDateTime getStartAt() { return startAt; }
    public OffsetDateTime getEndAt() { return endAt; }
    public Integer getCapacity() { return capacity; }
    public Integer getBookedCount() { return bookedCount; }
    public String getStatus() { return status; }

    public void setCourseId(Long v) { this.courseId = v; }
    public void setStudioId(Long v) { this.studioId = v; }
    public void setCoachId(Long v) { this.coachId = v; }
    public void setClassroomName(String v) { this.classroomName = v; }
    public void setStartAt(OffsetDateTime v) { this.startAt = v; }
    public void setEndAt(OffsetDateTime v) { this.endAt = v; }
    public void setCapacity(Integer v) { this.capacity = v; }
    public void setBookedCount(Integer v) { this.bookedCount = v; }
    public void setStatus(String v) { this.status = v; }
}
