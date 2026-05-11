package com.bitdance.catalog.dto;

import java.math.BigDecimal;

public record CourseDetail(
    Long id,
    Long studioId,
    Long coachId,
    Long danceStyleId,
    String courseName,
    String difficultyLevel,
    String targetAudience,
    BigDecimal priceAmount,
    Integer durationMinutes,
    String intensityLevel,
    String courseType,
    Boolean zeroBasicFriendly,
    String description,
    Long coverAssetId,
    String status,
    boolean favored
) {}
