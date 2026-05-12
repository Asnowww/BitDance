package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "topic_tag")
public class TopicTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_code", nullable = false, length = 64)
    private String topicCode;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "creator_user_id")
    private Long creatorUserId;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.FALSE;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "active";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String v) { this.topicCode = v; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String v) { this.topicName = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long v) { this.creatorUserId = v; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean v) { this.isSystem = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
