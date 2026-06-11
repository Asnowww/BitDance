package com.bitdance.community.dto;

import java.time.OffsetDateTime;

public record MediaAssetDto(
    Long id,
    String mediaType,
    String url,
    String originalFilename,
    String mimeType,
    Long fileSize,
    Integer sortOrder,
    OffsetDateTime createdAt
) {}
