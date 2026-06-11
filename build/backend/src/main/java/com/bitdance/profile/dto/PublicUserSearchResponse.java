package com.bitdance.profile.dto;

import java.util.List;

public record PublicUserSearchResponse(
    List<PublicUserProfileDto> list,
    int page,
    int pageSize,
    long total
) {}
