package com.bitdance.media.dto;

public record MediaAssetDto(
    Long assetId,
    String fileName,
    String mimeType,
    Long size,
    String contentUrl
) {}
