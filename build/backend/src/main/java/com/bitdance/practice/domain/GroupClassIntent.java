package com.bitdance.practice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_class_intent")
public class GroupClassIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "dance_style_id", nullable = false)
    private Long danceStyleId;

    @Column(name = "preferred_time_note", columnDefinition = "text")
    private String preferredTimeNote;

    @Column(name = "target_people_count", nullable = false)
    private Integer targetPeopleCount = 4;

    @Column(name = "current_people_count", nullable = false)
    private Integer currentPeopleCount = 1;

    @Column(name = "intent_status", nullable = false, length = 16)
    private String intentStatus = "collecting";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long v) { this.creatorUserId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public String getPreferredTimeNote() { return preferredTimeNote; }
    public void setPreferredTimeNote(String v) { this.preferredTimeNote = v; }
    public Integer getTargetPeopleCount() { return targetPeopleCount; }
    public void setTargetPeopleCount(Integer v) { this.targetPeopleCount = v; }
    public Integer getCurrentPeopleCount() { return currentPeopleCount; }
    public void setCurrentPeopleCount(Integer v) { this.currentPeopleCount = v; }
    public String getIntentStatus() { return intentStatus; }
    public void setIntentStatus(String v) { this.intentStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
