package com.bitdance.media.dto;

import java.time.OffsetDateTime;

public record MediaAssetDto(
    Long id,
    String assetType,
    String bizType,
    String originFileName,
    String mimeType,
    Long fileSize,
    String url,
    OffsetDateTime createdAt
) {}
