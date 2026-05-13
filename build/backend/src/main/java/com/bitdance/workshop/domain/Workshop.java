package com.bitdance.workshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "workshop")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "studio_id")
    private Long studioId;

    @Column(name = "coach_id")
    private Long coachId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "workshop_name", nullable = false, length = 200)
    private String workshopName;

    @Column(name = "cover_asset_id")
    private Long coverAssetId;

    @Column(name = "intro", columnDefinition = "text")
    private String intro;

    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "location_name", nullable = false, length = 200)
    private String locationName;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "price_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAmount = BigDecimal.ZERO;

    @Column(name = "min_people", nullable = false)
    private Integer minPeople = 1;

    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    @Column(name = "signup_deadline")
    private OffsetDateTime signupDeadline;

    @Column(name = "source_type", nullable = false, length = 16)
    private String sourceType = "studio";

    @Column(name = "audit_status", nullable = false, length = 16)
    private String auditStatus = "pending";

    @Column(name = "publish_status", nullable = false, length = 16)
    private String publishStatus = "draft";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long v) { this.creatorUserId = v; }
    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getCoachId() { return coachId; }
    public void setCoachId(Long v) { this.coachId = v; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long v) { this.cityId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String v) { this.workshopName = v; }
    public Long getCoverAssetId() { return coverAssetId; }
    public void setCoverAssetId(Long v) { this.coverAssetId = v; }
    public String getIntro() { return intro; }
    public void setIntro(String v) { this.intro = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String v) { this.locationName = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal v) { this.priceAmount = v; }
    public Integer getMinPeople() { return minPeople; }
    public void setMinPeople(Integer v) { this.minPeople = v; }
    public Integer getMaxPeople() { return maxPeople; }
    public void setMaxPeople(Integer v) { this.maxPeople = v; }
    public OffsetDateTime getSignupDeadline() { return signupDeadline; }
    public void setSignupDeadline(OffsetDateTime v) { this.signupDeadline = v; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String v) { this.auditStatus = v; }
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String v) { this.publishStatus = v; }
}
