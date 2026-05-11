package com.bitdance.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "user_dance_preference")
public class UserDancePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dance_style_id", nullable = false)
    private Long danceStyleId;

    @Column(name = "skill_level", length = 32)
    private String skillLevel;

    @Column(name = "preference_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal preferenceWeight = new BigDecimal("1.00");

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = Boolean.FALSE;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String v) { this.skillLevel = v; }
    public BigDecimal getPreferenceWeight() { return preferenceWeight; }
    public void setPreferenceWeight(BigDecimal v) { this.preferenceWeight = v; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean v) { this.isPrimary = v; }
}
