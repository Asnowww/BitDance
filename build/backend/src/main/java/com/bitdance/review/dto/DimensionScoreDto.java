package com.bitdance.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DimensionScoreDto(
    @NotBlank @Size(max = 64) String code,
    @NotBlank @Size(max = 100) String name,
    @Min(1) @Max(5) Short score
) {}
