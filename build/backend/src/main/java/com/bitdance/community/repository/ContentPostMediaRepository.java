package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentPostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContentPostMediaRepository extends JpaRepository<ContentPostMedia, Long> {

    List<ContentPostMedia> findByContentPostIdInAndMediaStatusOrderBySortOrderAscIdAsc(
        Collection<Long> contentPostIds,
        String mediaStatus
    );

    List<ContentPostMedia> findByContentPostIdAndMediaStatusOrderBySortOrderAscIdAsc(
        Long contentPostId,
        String mediaStatus
    );

    List<ContentPostMedia> findByIdIn(Collection<Long> ids);
}
