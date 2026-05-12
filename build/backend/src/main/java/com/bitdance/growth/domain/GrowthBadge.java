package com.bitdance.growth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "growth_badge")
public class GrowthBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "source_type", length = 32)
    private String sourceType;

    @Column(name = "source_ref_id")
    private Long sourceRefId;

    @Column(name = "awarded_at", nullable = false)
    private OffsetDateTime awardedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBadgeId() { return badgeId; }
    public String getSourceType() { return sourceType; }
    public Long getSourceRefId() { return sourceRefId; }
    public OffsetDateTime getAwardedAt() { return awardedAt; }
}
