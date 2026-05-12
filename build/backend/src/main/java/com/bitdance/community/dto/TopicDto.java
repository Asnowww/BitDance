package com.bitdance.community.dto;

public record TopicDto(
    Long id,
    String topicCode,
    String topicName,
    Long postCount,
    boolean hot
) {}
