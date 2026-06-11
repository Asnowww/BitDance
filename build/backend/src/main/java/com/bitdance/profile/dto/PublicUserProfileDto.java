package com.bitdance.profile.dto;

import java.util.List;

public record PublicUserProfileDto(
    Long userId,
    String nickname,
    Long avatarAssetId,
    String bio,
    Long cityId,
    String currentLevel,
    String learningGoal,
    List<StylePreferenceDto> styles,
    PublicUserAccessDto access
) {}
