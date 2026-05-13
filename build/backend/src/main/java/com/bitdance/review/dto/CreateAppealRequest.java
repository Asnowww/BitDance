package com.bitdance.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppealRequest(
    @NotNull Long reviewId,
    @NotBlank @Size(min = 5, max = 2000) String appealReason,
    @Size(max = 2000) String evidenceNote
) {}
