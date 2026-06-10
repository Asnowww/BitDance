package com.bitdance.media.repository;

import com.bitdance.media.domain.MediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
    List<MediaAttachment> findByTargetTypeAndTargetIdOrderBySortOrderAsc(String targetType, Long targetId);

    List<MediaAttachment> findByTargetTypeAndTargetIdInAndUsageTypeOrderByTargetIdAscSortOrderAsc(
        String targetType,
        Collection<Long> targetIds,
        String usageType
    );

    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);

    Optional<MediaAttachment> findByAssetIdAndTargetTypeAndTargetIdAndUsageType(
        Long assetId,
        String targetType,
        Long targetId,
        String usageType
    );
}
