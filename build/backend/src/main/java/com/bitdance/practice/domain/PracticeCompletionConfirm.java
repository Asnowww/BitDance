package com.bitdance.practice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "practice_completion_confirm")
public class PracticeCompletionConfirm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "practice_post_id", nullable = false)
    private Long practicePostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "confirmed_at", nullable = false)
    private OffsetDateTime confirmedAt;

    public Long getId() { return id; }
    public Long getPracticePostId() { return practicePostId; }
    public void setPracticePostId(Long v) { this.practicePostId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public OffsetDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(OffsetDateTime v) { this.confirmedAt = v; }
}
