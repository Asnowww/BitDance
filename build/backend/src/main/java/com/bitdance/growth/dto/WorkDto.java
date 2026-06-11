package com.bitdance.growth.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.bitdance.media.dto.MediaAssetDto;

public record WorkDto(
    Long id,
    Long userId,
    Long danceStyleId,
    String workTitle,
    String workDescription,
    Long coverAssetId,
    Boolean isPublic,
    OffsetDateTime createdAt,
    String coverUrl,
    List<MediaAssetDto> mediaAssets
) {}
