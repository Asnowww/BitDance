package com.bitdance.practice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupClassIntentRequest(
    @NotNull Long studioId,
    @NotNull Long danceStyleId,
    @Size(max = 1000) String preferredTimeNote,
    @Min(2) @Max(50) Integer targetPeopleCount
) {}
