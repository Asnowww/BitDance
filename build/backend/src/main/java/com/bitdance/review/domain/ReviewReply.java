package com.bitdance.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "review_reply")
public class ReviewReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "replier_user_id", nullable = false)
    private Long replierUserId;

    @Column(name = "reply_content", nullable = false, columnDefinition = "text")
    private String replyContent;

    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial = Boolean.FALSE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long v) { this.reviewId = v; }
    public Long getReplierUserId() { return replierUserId; }
    public void setReplierUserId(Long v) { this.replierUserId = v; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String v) { this.replyContent = v; }
    public Boolean getIsOfficial() { return isOfficial; }
    public void setIsOfficial(Boolean v) { this.isOfficial = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
