package com.bitdance.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MerchantCourseRequest(
    @NotNull Long studioId,
    Long coachId,
    @NotNull Long danceStyleId,
    @NotBlank String courseName,
    @NotBlank String difficultyLevel,
    BigDecimal priceAmount,
    Boolean trialEnabled,
    BigDecimal trialPriceAmount,
    Integer trialCapacity,
    @Positive Integer durationMinutes,
    String intensityLevel,
    String courseType,
    Boolean zeroBasicFriendly,
    String description,
    Long coverAssetId
) {}
