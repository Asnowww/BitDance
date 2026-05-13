package com.bitdance.merchant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InviteCoachRequest(
    @NotNull Long studioId,
    @NotNull Long coachId,
    @NotNull @Pattern(regexp = "full_time|signed|independent") String relationType,
    @Pattern(regexp = "ratio|fixed") String settlementMode,
    BigDecimal settlementRatio,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
