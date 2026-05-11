package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

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
    private String targetAudience;

    @Column(name = "price_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAmount;

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
    public String getTargetAudience() { return targetAudience; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getIntensityLevel() { return intensityLevel; }
    public String getCourseType() { return courseType; }
    public Boolean getZeroBasicFriendly() { return zeroBasicFriendly; }
    public String getDescription() { return description; }
    public Long getCoverAssetId() { return coverAssetId; }
    public String getStatus() { return status; }
}
