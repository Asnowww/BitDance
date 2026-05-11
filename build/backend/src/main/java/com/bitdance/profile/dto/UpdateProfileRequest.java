package com.bitdance.profile.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateProfileRequest(
    @Size(max = 100) String nickname,
    Long avatarAssetId,
    String gender,
    LocalDate birthday,
    @Size(max = 1000) String bio,
    Long cityId,
    String currentLevel,
    @Size(max = 1000) String learningGoal,
    List<StylePreferenceDto> styles,
    PrivacyDto privacy
) {}
