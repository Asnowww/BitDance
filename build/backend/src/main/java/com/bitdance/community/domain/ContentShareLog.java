package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "content_share_log")
public class ContentShareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_post_id", nullable = false)
    private Long contentPostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "share_channel", nullable = false, length = 32)
    private String shareChannel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getContentPostId() { return contentPostId; }
    public void setContentPostId(Long v) { this.contentPostId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getShareChannel() { return shareChannel; }
    public void setShareChannel(String v) { this.shareChannel = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
