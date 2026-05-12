package com.bitdance.buddy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * 搭子关系：使用 (user_id_low, user_id_high) 有序双键存储无向关系。
 * 写入前调用方必须保证 user_id_low < user_id_high（service 层 sort）。
 * Schema chk_buddy_relation_user_pair 兜底，违反约束会 PSQLException。
 */
@Entity
@Table(name = "buddy_relation")
public class BuddyRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_low", nullable = false)
    private Long userIdLow;

    @Column(name = "user_id_high", nullable = false)
    private Long userIdHigh;

    @Column(name = "source_practice_post_id", nullable = false)
    private Long sourcePracticePostId;

    @Column(name = "relation_status", nullable = false, length = 16)
    private String relationStatus = "active";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserIdLow() { return userIdLow; }
    public void setUserIdLow(Long v) { this.userIdLow = v; }
    public Long getUserIdHigh() { return userIdHigh; }
    public void setUserIdHigh(Long v) { this.userIdHigh = v; }
    public Long getSourcePracticePostId() { return sourcePracticePostId; }
    public void setSourcePracticePostId(Long v) { this.sourcePracticePostId = v; }
    public String getRelationStatus() { return relationStatus; }
    public void setRelationStatus(String v) { this.relationStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
