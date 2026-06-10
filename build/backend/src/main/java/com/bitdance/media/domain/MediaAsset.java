package com.bitdance.media.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "media_asset")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_type", nullable = false, length = 16)
    private String assetType;

    @Column(name = "biz_type", nullable = false, length = 32)
    private String bizType;

    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider = "db";

    @Column(name = "storage_type", nullable = false, length = 16)
    private String storageType = "db";

    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName = "db";

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "origin_file_name", nullable = false, length = 255)
    private String originFileName;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content")
    private byte[] content;

    @Column(name = "uploader_user_id")
    private Long uploaderUserId;

    @Column(name = "audit_status", nullable = false, length = 16)
    private String auditStatus = "approved";

    @Column(name = "is_public", nullable = false)
    private Boolean publicAsset = Boolean.FALSE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String v) { this.assetType = v; }
    public String getBizType() { return bizType; }
    public void setBizType(String v) { this.bizType = v; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String v) { this.storageProvider = v; }
    public String getStorageType() { return storageType; }
    public void setStorageType(String v) { this.storageType = v; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String v) { this.bucketName = v; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String v) { this.objectKey = v; }
    public String getOriginFileName() { return originFileName; }
    public void setOriginFileName(String v) { this.originFileName = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { this.fileName = v; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String v) { this.mimeType = v; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long v) { this.fileSize = v; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] v) { this.content = v; }
    public Long getUploaderUserId() { return uploaderUserId; }
    public void setUploaderUserId(Long v) { this.uploaderUserId = v; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String v) { this.auditStatus = v; }
    public Boolean getPublicAsset() { return publicAsset; }
    public void setPublicAsset(Boolean v) { this.publicAsset = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
