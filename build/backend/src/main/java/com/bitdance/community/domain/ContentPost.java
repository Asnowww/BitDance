package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "content_post")
public class ContentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_user_id", nullable = false)
    private Long authorUserId;

    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "related_course_id")
    private Long relatedCourseId;

    @Column(name = "related_workshop_id")
    private Long relatedWorkshopId;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "geo_hash", length = 12)
    private String geoHash;

    @Column(name = "post_type", nullable = false, length = 16)
    private String postType = "note";

    @Column(name = "content_text", nullable = false, columnDefinition = "text")
    private String contentText;

    @Column(name = "visibility", nullable = false, length = 16)
    private String visibility = "public";

    @Column(name = "post_status", nullable = false, length = 16)
    private String postStatus = "published";

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public Long getRelatedCourseId() { return relatedCourseId; }
    public void setRelatedCourseId(Long v) { this.relatedCourseId = v; }
    public Long getRelatedWorkshopId() { return relatedWorkshopId; }
    public void setRelatedWorkshopId(Long v) { this.relatedWorkshopId = v; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long v) { this.cityId = v; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String v) { this.locationName = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public String getGeoHash() { return geoHash; }
    public void setGeoHash(String v) { this.geoHash = v; }
    public String getPostType() { return postType; }
    public void setPostType(String v) { this.postType = v; }
    public String getContentText() { return contentText; }
    public void setContentText(String v) { this.contentText = v; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String v) { this.visibility = v; }
    public String getPostStatus() { return postStatus; }
    public void setPostStatus(String v) { this.postStatus = v; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime v) { this.publishedAt = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
