package com.bitdance.favorite.dto;

import java.time.OffsetDateTime;

public record FavoriteDto(
    Long id,
    String targetType,
    Long targetId,
    OffsetDateTime createdAt,
    FavoriteCardDto card
) {}
