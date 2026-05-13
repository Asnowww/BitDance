package com.bitdance.merchant.dto;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCoachRelationRequest(
    @Pattern(regexp = "pending|active|inactive|terminated") String relationStatus,
    @Pattern(regexp = "full_time|signed|independent") String relationType,
    BigDecimal settlementRatio,
    LocalDate effectiveTo
) {}
