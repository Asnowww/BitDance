package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "studio")
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_code", nullable = false, length = 32)
    private String studioCode;

    @Column(name = "studio_name", nullable = false, length = 150)
    private String studioName;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "business_district_id")
    private Long businessDistrictId;

    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "transport_info", columnDefinition = "text")
    private String transportInfo;

    @Column(name = "navigation_address", columnDefinition = "text")
    private String navigationAddress;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "geo_hash", length = 12)
    private String geoHash;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "intro", columnDefinition = "text")
    private String intro;

    @Column(name = "business_hours", columnDefinition = "jsonb")
    private String businessHours;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "cover_asset_id")
    private Long coverAssetId;

    @Column(name = "claim_status", nullable = false, length = 16)
    private String claimStatus;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getStudioCode() { return studioCode; }
    public String getStudioName() { return studioName; }
    public String getBrandName() { return brandName; }
    public Long getCityId() { return cityId; }
    public Long getBusinessDistrictId() { return businessDistrictId; }
    public String getAddress() { return address; }
    public String getTransportInfo() { return transportInfo; }
    public String getNavigationAddress() { return navigationAddress; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public String getGeoHash() { return geoHash; }
    public String getContactPhone() { return contactPhone; }
    public String getIntro() { return intro; }
    public String getBusinessHours() { return businessHours; }
    public String getSourceType() { return sourceType; }
    public Long getCoverAssetId() { return coverAssetId; }
    public String getClaimStatus() { return claimStatus; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setStudioCode(String v) { this.studioCode = v; }
    public void setStudioName(String v) { this.studioName = v; }
    public void setBrandName(String v) { this.brandName = v; }
    public void setCityId(Long v) { this.cityId = v; }
    public void setBusinessDistrictId(Long v) { this.businessDistrictId = v; }
    public void setAddress(String v) { this.address = v; }
    public void setTransportInfo(String v) { this.transportInfo = v; }
    public void setNavigationAddress(String v) { this.navigationAddress = v; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public void setGeoHash(String v) { this.geoHash = v; }
    public void setContactPhone(String v) { this.contactPhone = v; }
    public void setIntro(String v) { this.intro = v; }
    public void setBusinessHours(String v) { this.businessHours = v; }
    public void setSourceType(String v) { this.sourceType = v; }
    public void setCoverAssetId(Long v) { this.coverAssetId = v; }
    public void setClaimStatus(String v) { this.claimStatus = v; }
    public void setStatus(String v) { this.status = v; }
}
