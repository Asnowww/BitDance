package com.bitdance.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * 审计日志。
 *
 * Schema 注：
 *  - actor_role_code 是 varchar(50) 快照字段，不强依赖 sys_role
 *  - before_data / after_data 是 jsonb，本期仅以 String 占位且 insertable/updatable=false 让 JPA 忽略写
 *    （H2 无法识别 jsonb，避免单测拉起 JPA 时挂）。真库阶段（BE-015）改 @JdbcTypeCode(SqlTypes.JSON)
 *  - ip_address 是 inet 类型，本期不写
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role_code", length = 50)
    private String actorRoleCode;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "action_code", nullable = false, length = 64)
    private String actionCode;

    @Column(name = "before_data", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String afterData;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long v) { this.actorUserId = v; }
    public String getActorRoleCode() { return actorRoleCode; }
    public void setActorRoleCode(String v) { this.actorRoleCode = v; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String v) { this.targetType = v; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long v) { this.targetId = v; }
    public String getActionCode() { return actionCode; }
    public void setActionCode(String v) { this.actionCode = v; }
    public String getBeforeData() { return beforeData; }
    public String getAfterData() { return afterData; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String v) { this.userAgent = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
