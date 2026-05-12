package com.bitdance.community.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PostDto(
    Long id,
    Long authorUserId,
    String postType,
    String contentText,
    Long danceStyleId,
    Long relatedCourseId,
    Long relatedWorkshopId,
    Long cityId,
    String locationName,
    BigDecimal longitude,
    BigDecimal latitude,
    String visibility,
    String postStatus,
    OffsetDateTime publishedAt,
    List<TopicDto> topics,
    long likeCount,
    long commentCount,
    boolean liked
) {}
