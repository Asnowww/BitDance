package com.bitdance.profile.dto;

import java.time.LocalDate;
import java.util.List;

public record ProfileResponse(
    Long userId,
    String nickname,
    Long avatarAssetId,
    String gender,
    LocalDate birthday,
    String bio,
    Long cityId,
    String currentLevel,
    String learningGoal,
    List<StylePreferenceDto> styles,
    PrivacyDto privacy
) {}
