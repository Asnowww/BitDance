package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ContentCommentRepository extends JpaRepository<ContentComment, Long> {

    List<ContentComment> findByContentPostIdAndCommentStatusOrderByIdAsc(
        Long postId, String commentStatus
    );

    long countByContentPostIdAndCommentStatus(Long postId, String commentStatus);

    @Query("""
        select c.contentPostId as postId, count(c.id) as cnt
        from ContentComment c
        where c.contentPostId in :postIds and c.commentStatus = 'published'
        group by c.contentPostId
        """)
    List<Map<String, Object>> countGroupedByPostIds(@Param("postIds") List<Long> postIds);
}
