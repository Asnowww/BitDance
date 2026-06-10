package com.bitdance.review.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ReviewDto(
    Long id,
    Long userId,
    String targetType,
    Long targetId,
    BigDecimal overallScore,
    String contentText,
    Boolean isVerified,
    String verifiedSourceType,
    BigDecimal weightFactor,
    String reviewStatus,
    Short riskLevel,
    Integer helpfulCount,
    Boolean isPinned,
    OffsetDateTime publishedAt,
    List<DimensionScoreDto> dimensions
) {}
