package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ContentLikeRepository extends JpaRepository<ContentLike, ContentLike.PK> {

    long countByContentPostId(Long postId);

    @Query("""
        select l.contentPostId as postId, count(l.userId) as cnt
        from ContentLike l
        where l.contentPostId in :postIds
        group by l.contentPostId
        """)
    List<Map<String, Object>> countGroupedByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
        select l.contentPostId from ContentLike l
        where l.userId = :userId and l.contentPostId in :postIds
        """)
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
