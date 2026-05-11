package com.bitdance.practice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreatePracticeRequest(
    @NotNull Long danceStyleId,
    @NotNull Long cityId,
    Long studioId,
    @NotBlank @Size(max = 200) String locationName,
    @Size(max = 1000) String locationAddress,
    BigDecimal longitude,
    BigDecimal latitude,
    String skillLevel,
    @Min(1) @Max(50) Integer expectedPeopleMin,
    @Min(1) @Max(50) Integer expectedPeopleMax,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 2000) String description
) {}
