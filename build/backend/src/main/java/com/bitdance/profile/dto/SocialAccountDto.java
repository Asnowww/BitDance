package com.bitdance.profile.dto;

public record SocialAccountDto(
    Long id,
    Long userId,
    String platform,
    String accountName,
    String profileUrl,
    Boolean isPublic
) {
}
