package com.bitdance.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToggleFavoriteRequest(
    @NotBlank String targetType,
    @NotNull Long targetId
) {}
