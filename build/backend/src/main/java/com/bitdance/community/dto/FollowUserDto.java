package com.bitdance.community.dto;

import java.time.OffsetDateTime;

public record FollowUserDto(
    Long userId,
    String name,
    String avatar,
    boolean following,
    long followerCount,
    long followeeCount,
    OffsetDateTime followedAt
) {}
