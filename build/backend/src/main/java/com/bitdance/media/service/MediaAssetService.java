package com.bitdance.media.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.media.domain.MediaAsset;
import com.bitdance.media.dto.MediaAssetDto;
import com.bitdance.media.repository.MediaAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaAssetService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_MIME = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOC_MIME = Set.of("application/pdf");

    private final MediaAssetRepository repo;

    public MediaAssetService(MediaAssetRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public MediaAssetDto upload(Long userId, MultipartFile file, String bizType, Boolean publicAsset) {
        if (file == null || file.isEmpty()) {
            throw new BizException("FILE_EMPTY", "上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("FILE_TOO_LARGE", "单文件最大 5MB");
        }
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!IMAGE_MIME.contains(mime) && !DOC_MIME.contains(mime)) {
            throw new BizException("FILE_TYPE_NOT_ALLOWED", "仅支持 jpg/png/webp/pdf");
        }
        String original = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String assetType = DOC_MIME.contains(mime) ? "document" : "image";
        String objectKey = UUID.randomUUID() + "-" + sanitize(original);

        MediaAsset asset = new MediaAsset();
        asset.setAssetType(assetType);
        asset.setBizType(bizType == null || bizType.isBlank() ? "m7" : bizType);
        asset.setObjectKey(objectKey);
        asset.setOriginFileName(original);
        asset.setFileName(original);
        asset.setMimeType(mime);
        asset.setFileSize(file.getSize());
        asset.setUploaderUserId(userId);
        asset.setPublicAsset(Boolean.TRUE.equals(publicAsset));
        try {
            asset.setContent(file.getBytes());
        } catch (IOException e) {
            throw new BizException("FILE_READ_FAILED", "读取上传文件失败");
        }
        MediaAsset saved = repo.save(asset);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public MediaAsset loadContent(Long id) {
        MediaAsset asset = repo.findById(id)
            .orElseThrow(() -> new BizException("MEDIA_NOT_FOUND", "文件不存在"));
        if (asset.getContent() == null) {
            throw new BizException("MEDIA_CONTENT_EMPTY", "文件内容不存在");
        }
        return asset;
    }

    private MediaAssetDto toDto(MediaAsset a) {
        return new MediaAssetDto(
            a.getId(),
            a.getFileName(),
            a.getMimeType(),
            a.getFileSize(),
            "/api/public/media-assets/" + a.getId() + "/content"
        );
    }

    private String sanitize(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
