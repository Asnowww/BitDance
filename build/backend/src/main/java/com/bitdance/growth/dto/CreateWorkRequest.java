package com.bitdance.growth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkRequest(
    Long danceStyleId,
    @NotBlank @Size(max = 200) String workTitle,
    @Size(max = 2000) String workDescription,
    Long coverAssetId,
    Boolean isPublic,
    java.util.List<Long> mediaAssetIds
) {}
