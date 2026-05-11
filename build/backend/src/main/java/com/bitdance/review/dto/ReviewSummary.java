package com.bitdance.review.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewSummary(
    String targetType,
    Long targetId,
    long count,
    long verifiedCount,
    BigDecimal weightedAvgScore,
    Map<String, BigDecimal> dimensionAvg
) {}
