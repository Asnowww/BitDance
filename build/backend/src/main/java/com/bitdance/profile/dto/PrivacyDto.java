package com.bitdance.profile.dto;

public record PrivacyDto(
    String profileVisibility,
    String growthVisibility,
    String practiceVisibility,
    String contentVisibility
) {}
