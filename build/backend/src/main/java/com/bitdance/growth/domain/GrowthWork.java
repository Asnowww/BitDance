package com.bitdance.growth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "growth_work")
public class GrowthWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "work_title", nullable = false, length = 200)
    private String workTitle;

    @Column(name = "work_description", columnDefinition = "text")
    private String workDescription;

    @Column(name = "cover_asset_id")
    private Long coverAssetId;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = Boolean.TRUE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public String getWorkTitle() { return workTitle; }
    public void setWorkTitle(String v) { this.workTitle = v; }
    public String getWorkDescription() { return workDescription; }
    public void setWorkDescription(String v) { this.workDescription = v; }
    public Long getCoverAssetId() { return coverAssetId; }
    public void setCoverAssetId(Long v) { this.coverAssetId = v; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean v) { this.isPublic = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
