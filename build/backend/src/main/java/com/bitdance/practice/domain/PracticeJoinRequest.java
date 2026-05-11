package com.bitdance.practice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "practice_join_request")
public class PracticeJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "practice_post_id", nullable = false)
    private Long practicePostId;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(name = "join_status", nullable = false, length = 16)
    private String joinStatus = "pending";

    @Column(name = "join_message", columnDefinition = "text")
    private String joinMessage;

    @Column(name = "acted_by_user_id")
    private Long actedByUserId;

    @Column(name = "acted_at")
    private OffsetDateTime actedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getPracticePostId() { return practicePostId; }
    public void setPracticePostId(Long v) { this.practicePostId = v; }
    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long v) { this.applicantUserId = v; }
    public String getJoinStatus() { return joinStatus; }
    public void setJoinStatus(String v) { this.joinStatus = v; }
    public String getJoinMessage() { return joinMessage; }
    public void setJoinMessage(String v) { this.joinMessage = v; }
    public Long getActedByUserId() { return actedByUserId; }
    public void setActedByUserId(Long v) { this.actedByUserId = v; }
    public OffsetDateTime getActedAt() { return actedAt; }
    public void setActedAt(OffsetDateTime v) { this.actedAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
