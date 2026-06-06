package com.bitdance.review.service;

import com.bitdance.media.domain.MediaAsset;
import com.bitdance.media.domain.MediaAttachment;
import com.bitdance.media.repository.MediaAssetRepository;
import com.bitdance.media.repository.MediaAttachmentRepository;
import com.bitdance.review.dto.ReviewMediaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class ReviewMediaService {

    private static final String TARGET_TYPE = "review";
    private static final String USAGE_TYPE = "review_media";
    private static final String EXTERNAL_BUCKET = "external-url";
    private static final List<String> FALLBACK_IMAGE_URLS = List.of(
        "https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=960&q=80&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=960&q=80&auto=format&fit=crop"
    );

    private final MediaAssetRepository assetRepo;
    private final MediaAttachmentRepository attachmentRepo;

    public ReviewMediaService(MediaAssetRepository assetRepo, MediaAttachmentRepository attachmentRepo) {
        this.assetRepo = assetRepo;
        this.attachmentRepo = attachmentRepo;
    }

    @Transactional
    public List<ReviewMediaDto> attachReviewMedia(Long reviewId, Long uploaderUserId, List<ReviewMediaDto> media) {
        if (media == null || media.isEmpty()) return List.of();
        int limit = Math.min(media.size(), 6);
        for (int i = 0; i < limit; i++) {
            ReviewMediaDto item = media.get(i);
            MediaAsset asset = resolveAsset(item, uploaderUserId, reviewId, i);
            MediaAttachment attachment = attachmentRepo
                .findByAssetIdAndTargetTypeAndTargetIdAndUsageType(asset.getId(), TARGET_TYPE, reviewId, USAGE_TYPE)
                .orElseGet(MediaAttachment::new);
            attachment.setAssetId(asset.getId());
            attachment.setTargetType(TARGET_TYPE);
            attachment.setTargetId(reviewId);
            attachment.setUsageType(USAGE_TYPE);
            attachment.setSortOrder(i);
            attachmentRepo.save(attachment);
        }
        return mediaForReviews(List.of(reviewId)).getOrDefault(reviewId, List.of());
    }

    @Transactional(readOnly = true)
    public Map<Long, List<ReviewMediaDto>> mediaForReviews(Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) return Map.of();
        List<MediaAttachment> attachments = attachmentRepo
            .findByTargetTypeAndTargetIdInAndUsageTypeOrderByTargetIdAscSortOrderAsc(
                TARGET_TYPE, reviewIds, USAGE_TYPE
            );
        Map<Long, MediaAsset> assets = assetRepo.findAllById(
            attachments.stream().map(MediaAttachment::getAssetId).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(MediaAsset::getId, a -> a));

        Map<Long, List<ReviewMediaDto>> grouped = new HashMap<>();
        for (MediaAttachment attachment : attachments) {
            MediaAsset asset = assets.get(attachment.getAssetId());
            if (asset == null) continue;
            grouped.computeIfAbsent(attachment.getTargetId(), k -> new java.util.ArrayList<>())
                .add(toDto(asset));
        }
        return grouped;
    }

    private MediaAsset resolveAsset(ReviewMediaDto item, Long uploaderUserId, Long reviewId, int index) {
        String type = normalizeType(item == null ? null : item.type());
        String url = normalizeUrl(item == null ? null : item.url(), reviewId, index);
        return assetRepo.findByBucketNameAndObjectKey(EXTERNAL_BUCKET, url)
            .orElseGet(() -> {
                MediaAsset asset = new MediaAsset();
                asset.setAssetType(type);
                asset.setBizType("review");
                asset.setStorageProvider("external");
                asset.setBucketName(EXTERNAL_BUCKET);
                asset.setObjectKey(url);
                asset.setOriginFileName(normalizeName(item == null ? null : item.name(), type, index));
                asset.setMimeType("video".equals(type) ? "video/mp4" : "image/jpeg");
                asset.setFileSize(item == null || item.size() == null ? 0L : item.size());
                asset.setUploaderUserId(uploaderUserId);
                asset.setAuditStatus("approved");
                asset.setIsPublic(Boolean.TRUE);
                return assetRepo.save(asset);
            });
    }

    private String normalizeType(String type) {
        return "video".equals(type) ? "video" : "image";
    }

    private String normalizeName(String name, String type, int index) {
        if (name != null && !name.isBlank()) return name.length() > 255 ? name.substring(0, 255) : name;
        return "review-media-" + (index + 1) + ("video".equals(type) ? ".mp4" : ".jpg");
    }

    private String normalizeUrl(String url, Long reviewId, int index) {
        if (url != null && (url.startsWith("http://") || url.startsWith("https://")) && url.length() <= 255) {
            return url;
        }
        // 未接对象存储时，前端 data URL 只用于本地预览；后端落库为稳定的公开模拟图片 URL。
        int bucket = Math.abs((reviewId + ":" + index + ":" + stableHash(url)).hashCode());
        return FALLBACK_IMAGE_URLS.get(bucket % FALLBACK_IMAGE_URLS.size());
    }

    private String stableHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(String.valueOf(value).hashCode());
        }
    }

    private ReviewMediaDto toDto(MediaAsset asset) {
        return new ReviewMediaDto(
            asset.getId(),
            asset.getAssetType(),
            asset.getObjectKey(),
            asset.getOriginFileName(),
            asset.getFileSize()
        );
    }
}
