package com.bitdance.media.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.media.domain.MediaAsset;
import com.bitdance.media.dto.MediaAssetDto;
import com.bitdance.media.repository.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaAssetService {

    private final MediaAssetRepository repo;
    private final Path uploadRoot;

    public MediaAssetService(
        MediaAssetRepository repo,
        @Value("${bitdance.media.upload-dir:uploads}") String uploadDir
    ) {
        this.repo = repo;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public MediaAssetDto upload(Long userId, MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException("INVALID_ARGUMENT", "上传文件不能为空");
        }
        String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String assetType = assetType(mime);
        if (!"image".equals(assetType) && !"video".equals(assetType)) {
            throw new BizException("INVALID_ARGUMENT", "仅支持图片或视频");
        }
        String original = StringUtils.hasText(file.getOriginalFilename())
            ? file.getOriginalFilename()
            : "upload";
        String ext = extension(original);
        String objectKey = userId + "/" + UUID.randomUUID() + ext;
        Path target = uploadRoot.resolve(objectKey).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BizException("INVALID_ARGUMENT", "文件路径非法");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new BizException("MEDIA_UPLOAD_FAILED", "文件保存失败");
        }

        MediaAsset asset = new MediaAsset();
        asset.setAssetType(assetType);
        asset.setBizType(StringUtils.hasText(bizType) ? bizType : "growth_work");
        asset.setStorageProvider("local");
        asset.setBucketName("uploads");
        asset.setObjectKey(objectKey.replace('\\', '/'));
        asset.setOriginFileName(original);
        asset.setMimeType(mime);
        asset.setFileSize(file.getSize());
        asset.setSha256(sha256(target));
        asset.setUploaderUserId(userId);
        asset.setAuditStatus("approved");
        asset.setIsPublic(true);
        return toDto(repo.save(asset));
    }

    @Transactional(readOnly = true)
    public MediaAssetDto detail(Long id) {
        return toDto(load(id));
    }

    @Transactional(readOnly = true)
    public Resource content(Long id) {
        MediaAsset asset = load(id);
        if (!Boolean.TRUE.equals(asset.getIsPublic())) {
            throw new BizException("FORBIDDEN", "媒体不可公开访问");
        }
        Path path = uploadRoot.resolve(asset.getObjectKey()).normalize();
        if (!path.startsWith(uploadRoot) || !Files.exists(path)) {
            throw new BizException("MEDIA_NOT_FOUND", "媒体文件不存在");
        }
        return new FileSystemResource(path);
    }

    public MediaAssetDto toDto(MediaAsset asset) {
        return new MediaAssetDto(
            asset.getId(), asset.getAssetType(), asset.getBizType(), asset.getOriginFileName(),
            asset.getMimeType(), asset.getFileSize(), url(asset.getId()), asset.getCreatedAt()
        );
    }

    public String url(Long id) {
        return "/api/public/media-assets/" + id + "/content";
    }

    private MediaAsset load(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new BizException("MEDIA_NOT_FOUND", "媒体不存在"));
    }

    private String assetType(String mime) {
        String lower = mime.toLowerCase(Locale.ROOT);
        if (lower.startsWith("image/")) return "image";
        if (lower.startsWith("video/")) return "video";
        return "document";
    }

    private String extension(String original) {
        int i = original.lastIndexOf('.');
        if (i < 0 || i == original.length() - 1) return "";
        return original.substring(i).replaceAll("[^A-Za-z0-9.]", "");
    }

    private String sha256(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) > 0) {
                digest.update(buf, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException ex) {
            return null;
        }
    }
}
