package com.bitdance.profile.dto;

public record PublicUserAccessDto(
    boolean profileVisible,
    boolean contentVisible,
    boolean practiceVisible,
    boolean growthVisible
) {}
