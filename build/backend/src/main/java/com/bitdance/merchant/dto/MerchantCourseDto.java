package com.bitdance.merchant.dto;

import java.math.BigDecimal;

public record MerchantCourseDto(
    Long id,
    Long studioId,
    Long coachId,
    Long danceStyleId,
    String courseName,
    String difficultyLevel,
    BigDecimal priceAmount,
    Boolean trialEnabled,
    BigDecimal trialPriceAmount,
    Integer trialCapacity,
    Integer durationMinutes,
    String intensityLevel,
    String courseType,
    Boolean zeroBasicFriendly,
    String description,
    Long coverAssetId,
    String status
) {}
