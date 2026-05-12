package com.bitdance.community.repository;

import com.bitdance.community.domain.TopicTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicTagRepository extends JpaRepository<TopicTag, Long> {
    Optional<TopicTag> findByTopicName(String topicName);
    List<TopicTag> findByStatusOrderByIdDesc(String status);
    List<TopicTag> findByIdIn(List<Long> ids);
}
