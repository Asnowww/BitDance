package com.bitdance.favorite.dto;

public record FavoriteCardDto(
    String title,
    String subtitle,
    String coverUrl,
    String path,
    String actionText
) {}
