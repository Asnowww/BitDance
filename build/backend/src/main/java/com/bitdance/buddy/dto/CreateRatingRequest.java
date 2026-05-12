package com.bitdance.buddy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRatingRequest(
    @NotNull Long toUserId,
    @NotNull @Min(1) @Max(5) Short punctuality,
    @NotNull @Min(1) @Max(5) Short friendliness,
    @NotNull @Min(1) @Max(5) Short skillMatch,
    @Size(max = 1000) String comment
) {}
