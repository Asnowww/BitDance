package com.bitdance.growth.dto;

import java.time.OffsetDateTime;

public record WorkDto(
    Long id,
    Long userId,
    Long danceStyleId,
    String workTitle,
    String workDescription,
    Long coverAssetId,
    Boolean isPublic,
    OffsetDateTime createdAt
) {}
