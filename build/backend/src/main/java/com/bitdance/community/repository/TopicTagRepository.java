package com.bitdance.community.repository;

import com.bitdance.community.domain.TopicTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicTagRepository extends JpaRepository<TopicTag, Long> {
    Optional<TopicTag> findByTopicName(String topicName);
    Optional<TopicTag> findByTopicNameAndStatus(String topicName, String status);
    Optional<TopicTag> findByTopicCodeAndStatus(String topicCode, String status);
    List<TopicTag> findByStatusOrderByIdDesc(String status);
    List<TopicTag> findByIdIn(List<Long> ids);

    @Query("""
        select t, count(link.contentPostId)
        from TopicTag t
        left join ContentPostTopic link on link.topicTagId = t.id
        where t.status = 'active'
          and (:q is null or lower(t.topicName) like lower(concat('%', :q, '%')))
        group by t
        order by count(link.contentPostId) desc, t.id desc
        """)
    List<Object[]> findActiveWithPostCount(@Param("q") String q, Pageable pageable);
}
