package com.bitdance.merchant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "studio_coach_relation")
public class StudioCoachRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "relation_type", nullable = false, length = 16)
    private String relationType;

    @Column(name = "relation_status", nullable = false, length = 16)
    private String relationStatus = "active";

    /** jsonb 字段；JPA 仅以 String 占位读取。 */
    @Column(name = "permission_scope", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String permissionScope;

    @Column(name = "settlement_mode", nullable = false, length = 16)
    private String settlementMode = "ratio";

    @Column(name = "settlement_ratio", precision = 5, scale = 2, nullable = false)
    private BigDecimal settlementRatio = BigDecimal.ZERO;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getCoachId() { return coachId; }
    public void setCoachId(Long v) { this.coachId = v; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String v) { this.relationType = v; }
    public String getRelationStatus() { return relationStatus; }
    public void setRelationStatus(String v) { this.relationStatus = v; }
    public String getPermissionScope() { return permissionScope; }
    public String getSettlementMode() { return settlementMode; }
    public void setSettlementMode(String v) { this.settlementMode = v; }
    public BigDecimal getSettlementRatio() { return settlementRatio; }
    public void setSettlementRatio(BigDecimal v) { this.settlementRatio = v; }
    public Long getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(Long v) { this.invitedByUserId = v; }
    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long v) { this.approvedByUserId = v; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate v) { this.effectiveFrom = v; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate v) { this.effectiveTo = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
