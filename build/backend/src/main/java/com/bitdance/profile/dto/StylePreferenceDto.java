package com.bitdance.profile.dto;

public record StylePreferenceDto(
    Long danceStyleId,
    String name,
    String skillLevel,
    Boolean isPrimary
) {}
