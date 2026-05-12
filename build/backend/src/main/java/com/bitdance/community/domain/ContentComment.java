package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "content_comment")
public class ContentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_post_id", nullable = false)
    private Long contentPostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "reply_to_user_id")
    private Long replyToUserId;

    @Column(name = "comment_text", nullable = false, columnDefinition = "text")
    private String commentText;

    @Column(name = "comment_status", nullable = false, length = 16)
    private String commentStatus = "published";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getContentPostId() { return contentPostId; }
    public void setContentPostId(Long v) { this.contentPostId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long v) { this.parentCommentId = v; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long v) { this.replyToUserId = v; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String v) { this.commentText = v; }
    public String getCommentStatus() { return commentStatus; }
    public void setCommentStatus(String v) { this.commentStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
