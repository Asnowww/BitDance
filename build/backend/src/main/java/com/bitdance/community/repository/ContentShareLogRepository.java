package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentShareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ContentShareLogRepository extends JpaRepository<ContentShareLog, Long> {

    long countByContentPostId(Long contentPostId);

    @Query("""
        select s.contentPostId as postId, count(s.id) as cnt
        from ContentShareLog s
        where s.contentPostId in :postIds
        group by s.contentPostId
        """)
    List<Map<String, Object>> countGroupedByPostIds(@Param("postIds") List<Long> postIds);
}
