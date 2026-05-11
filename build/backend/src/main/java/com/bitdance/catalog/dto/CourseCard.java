package com.bitdance.catalog.dto;

import java.math.BigDecimal;

public record CourseCard(
    Long id,
    Long studioId,
    Long coachId,
    Long danceStyleId,
    String courseName,
    String difficultyLevel,
    BigDecimal priceAmount,
    Integer durationMinutes,
    Boolean zeroBasicFriendly,
    Long coverAssetId
) {}
