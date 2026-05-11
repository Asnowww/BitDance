package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "coach")
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "intro", columnDefinition = "text")
    private String intro;

    @Column(name = "teaching_style", columnDefinition = "text")
    private String teachingStyle;

    /**
     * jsonb 字段。首版以原始字符串读出，前端 JSON.parse。
     * 真库联调阶段再换 @JdbcTypeCode(SqlTypes.JSON) 或 Hypersistence Utils 做严格映射。
     */
    @Column(name = "available_time_slots", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String availableTimeSlots;

    @Column(name = "certification_status", nullable = false, length = 16)
    private String certificationStatus;

    @Column(name = "home_studio_id")
    private Long homeStudioId;

    @Column(name = "cover_asset_id")
    private Long coverAssetId;

    @Column(name = "avg_rating", precision = 4, scale = 2, nullable = false)
    private BigDecimal avgRating = BigDecimal.ZERO;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getIntro() { return intro; }
    public String getTeachingStyle() { return teachingStyle; }
    public String getAvailableTimeSlots() { return availableTimeSlots; }
    public String getCertificationStatus() { return certificationStatus; }
    public Long getHomeStudioId() { return homeStudioId; }
    public Long getCoverAssetId() { return coverAssetId; }
    public BigDecimal getAvgRating() { return avgRating; }
}
