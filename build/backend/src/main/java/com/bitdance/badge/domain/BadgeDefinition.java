package com.bitdance.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "badge_definition")
public class BadgeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_code", nullable = false, length = 64)
    private String badgeCode;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "icon_asset_id")
    private Long iconAssetId;

    @Column(name = "rule_type", nullable = false, length = 32)
    private String ruleType;

    /**
     * jsonb 字段。本期仅以 String 读出（insertable/updatable=false 让 JPA 忽略写入），
     * BadgeRuleEngine 解析时用 ObjectMapper 反序列化。真库阶段（BE-015-c 部署后）
     * 通过 SQL 或 admin 工具维护。
     */
    @Column(name = "rule_config", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String ruleConfig;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "active";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getBadgeCode() { return badgeCode; }
    public void setBadgeCode(String v) { this.badgeCode = v; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String v) { this.badgeName = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Long getIconAssetId() { return iconAssetId; }
    public void setIconAssetId(Long v) { this.iconAssetId = v; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String v) { this.ruleType = v; }
    public String getRuleConfig() { return ruleConfig; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
