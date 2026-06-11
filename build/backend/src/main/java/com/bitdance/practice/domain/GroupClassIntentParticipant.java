package com.bitdance.practice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_class_intent_participant")
public class GroupClassIntentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intent_id", nullable = false)
    private Long intentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "participant_status", nullable = false, length = 16)
    private String participantStatus = "joined";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getIntentId() { return intentId; }
    public void setIntentId(Long v) { this.intentId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getParticipantStatus() { return participantStatus; }
    public void setParticipantStatus(String v) { this.participantStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
