package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.StringJoiner;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "coach_id")
    private Long coachId;

    @Column(name = "dance_style_id", nullable = false)
    private Long danceStyleId;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(name = "difficulty_level", nullable = false, length = 16)
    private String difficultyLevel;

    /** text[] 字段，由 native 查询读出后转换。 */
    @Column(name = "target_audience", columnDefinition = "text[]", insertable = false, updatable = false)
    private String[] targetAudience;

    @Column(name = "price_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "trial_enabled", nullable = false)
    private Boolean trialEnabled = Boolean.FALSE;

    @Column(name = "trial_price_amount", precision = 10, scale = 2)
    private BigDecimal trialPriceAmount;

    @Column(name = "trial_capacity")
    private Integer trialCapacity;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "intensity_level", length = 16)
    private String intensityLevel;

    @Column(name = "course_type", nullable = false, length = 16)
    private String courseType;

    @Column(name = "zero_basic_friendly", nullable = false)
    private Boolean zeroBasicFriendly = Boolean.FALSE;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "cover_asset_id")
    private Long coverAssetId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    public Long getId() { return id; }
    public Long getStudioId() { return studioId; }
    public Long getCoachId() { return coachId; }
    public Long getDanceStyleId() { return danceStyleId; }
    public String getCourseName() { return courseName; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public String getTargetAudience() {
        if (targetAudience == null || targetAudience.length == 0) return null;
        StringJoiner joiner = new StringJoiner(",");
        for (String item : targetAudience) {
            if (item != null && !item.isBlank()) joiner.add(item);
        }
        String value = joiner.toString();
        return value.isBlank() ? null : value;
    }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public Boolean getTrialEnabled() { return trialEnabled; }
    public BigDecimal getTrialPriceAmount() { return trialPriceAmount; }
    public Integer getTrialCapacity() { return trialCapacity; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getIntensityLevel() { return intensityLevel; }
    public String getCourseType() { return courseType; }
    public Boolean getZeroBasicFriendly() { return zeroBasicFriendly; }
    public String getDescription() { return description; }
    public Long getCoverAssetId() { return coverAssetId; }
    public String getStatus() { return status; }

    public void setStudioId(Long v) { this.studioId = v; }
    public void setCoachId(Long v) { this.coachId = v; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public void setCourseName(String v) { this.courseName = v; }
    public void setDifficultyLevel(String v) { this.difficultyLevel = v; }
    public void setPriceAmount(BigDecimal v) { this.priceAmount = v; }
    public void setTrialEnabled(Boolean v) { this.trialEnabled = v; }
    public void setTrialPriceAmount(BigDecimal v) { this.trialPriceAmount = v; }
    public void setTrialCapacity(Integer v) { this.trialCapacity = v; }
    public void setDurationMinutes(Integer v) { this.durationMinutes = v; }
    public void setIntensityLevel(String v) { this.intensityLevel = v; }
    public void setCourseType(String v) { this.courseType = v; }
    public void setZeroBasicFriendly(Boolean v) { this.zeroBasicFriendly = v; }
    public void setDescription(String v) { this.description = v; }
    public void setCoverAssetId(Long v) { this.coverAssetId = v; }
    public void setStatus(String v) { this.status = v; }
}
