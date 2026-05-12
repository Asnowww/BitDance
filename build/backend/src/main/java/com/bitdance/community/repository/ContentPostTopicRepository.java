package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentPostTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ContentPostTopicRepository extends JpaRepository<ContentPostTopic, ContentPostTopic.PK> {

    List<ContentPostTopic> findByContentPostId(Long contentPostId);

    @Modifying
    @Query("delete from ContentPostTopic t where t.contentPostId = :postId")
    int deleteByContentPostId(@Param("postId") Long postId);

    @Query("""
        select t.contentPostId as postId, t.topicTagId as topicId
        from ContentPostTopic t
        where t.contentPostId in :postIds
        """)
    List<Map<String, Object>> findByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
        select count(t.contentPostId) from ContentPostTopic t
        where t.topicTagId = :topicId
        """)
    long countByTopicId(@Param("topicId") Long topicId);
}
