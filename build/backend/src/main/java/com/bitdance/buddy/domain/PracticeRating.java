package com.bitdance.buddy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "practice_rating")
public class PracticeRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "practice_post_id", nullable = false)
    private Long practicePostId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(name = "punctuality_score", nullable = false)
    private Short punctualityScore;

    @Column(name = "friendliness_score", nullable = false)
    private Short friendlinessScore;

    @Column(name = "skill_match_score", nullable = false)
    private Short skillMatchScore;

    @Column(name = "rating_comment", columnDefinition = "text")
    private String ratingComment;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getPracticePostId() { return practicePostId; }
    public void setPracticePostId(Long v) { this.practicePostId = v; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long v) { this.fromUserId = v; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long v) { this.toUserId = v; }
    public Short getPunctualityScore() { return punctualityScore; }
    public void setPunctualityScore(Short v) { this.punctualityScore = v; }
    public Short getFriendlinessScore() { return friendlinessScore; }
    public void setFriendlinessScore(Short v) { this.friendlinessScore = v; }
    public Short getSkillMatchScore() { return skillMatchScore; }
    public void setSkillMatchScore(Short v) { this.skillMatchScore = v; }
    public String getRatingComment() { return ratingComment; }
    public void setRatingComment(String v) { this.ratingComment = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
