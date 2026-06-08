package com.bitdance.media.repository;

import com.bitdance.media.domain.MediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
    List<MediaAttachment> findByTargetTypeAndTargetIdOrderBySortOrderAsc(String targetType, Long targetId);
    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);
}
