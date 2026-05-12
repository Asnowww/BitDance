package com.bitdance.growth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "growth_checkin")
public class GrowthCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "studio_id")
    private Long studioId;

    @Column(name = "course_schedule_id")
    private Long courseScheduleId;

    @Column(name = "practice_post_id")
    private Long practicePostId;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "feeling_text", columnDefinition = "text")
    private String feelingText;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = Boolean.TRUE;

    @Column(name = "checkin_at", nullable = false)
    private OffsetDateTime checkinAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getCourseScheduleId() { return courseScheduleId; }
    public void setCourseScheduleId(Long v) { this.courseScheduleId = v; }
    public Long getPracticePostId() { return practicePostId; }
    public void setPracticePostId(Long v) { this.practicePostId = v; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer v) { this.durationMinutes = v; }
    public String getFeelingText() { return feelingText; }
    public void setFeelingText(String v) { this.feelingText = v; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean v) { this.isPublic = v; }
    public OffsetDateTime getCheckinAt() { return checkinAt; }
    public void setCheckinAt(OffsetDateTime v) { this.checkinAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
