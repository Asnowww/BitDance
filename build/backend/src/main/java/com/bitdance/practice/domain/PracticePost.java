package com.bitdance.practice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "practice_post")
public class PracticePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "dance_style_id", nullable = false)
    private Long danceStyleId;

    @Column(name = "studio_id")
    private Long studioId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "location_name", nullable = false, length = 200)
    private String locationName;

    @Column(name = "location_address", columnDefinition = "text")
    private String locationAddress;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "geo_hash", length = 12)
    private String geoHash;

    @Column(name = "skill_level", length = 32)
    private String skillLevel;

    @Column(name = "expected_people_min", nullable = false)
    private Integer expectedPeopleMin = 2;

    @Column(name = "expected_people_max", nullable = false)
    private Integer expectedPeopleMax = 4;

    @Column(name = "current_people_count", nullable = false)
    private Integer currentPeopleCount = 1;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "cancel_limit_hours", nullable = false)
    private Integer cancelLimitHours = 2;

    @Column(name = "post_status", nullable = false, length = 16)
    private String postStatus = "published";

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long v) { this.creatorUserId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long v) { this.cityId = v; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String v) { this.locationName = v; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String v) { this.locationAddress = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public String getGeoHash() { return geoHash; }
    public void setGeoHash(String v) { this.geoHash = v; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String v) { this.skillLevel = v; }
    public Integer getExpectedPeopleMin() { return expectedPeopleMin; }
    public void setExpectedPeopleMin(Integer v) { this.expectedPeopleMin = v; }
    public Integer getExpectedPeopleMax() { return expectedPeopleMax; }
    public void setExpectedPeopleMax(Integer v) { this.expectedPeopleMax = v; }
    public Integer getCurrentPeopleCount() { return currentPeopleCount; }
    public void setCurrentPeopleCount(Integer v) { this.currentPeopleCount = v; }
    public OffsetDateTime getStartAt() { return startAt; }
    public void setStartAt(OffsetDateTime v) { this.startAt = v; }
    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime v) { this.endAt = v; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime v) { this.expiresAt = v; }
    public Integer getCancelLimitHours() { return cancelLimitHours; }
    public void setCancelLimitHours(Integer v) { this.cancelLimitHours = v; }
    public String getPostStatus() { return postStatus; }
    public void setPostStatus(String v) { this.postStatus = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
