package com.bitdance.coachops.dto;

import jakarta.validation.constraints.Size;

public record UpdateCoachProfileRequest(
    @Size(max = 100) String displayName,
    @Size(max = 2000) String intro,
    @Size(max = 2000) String teachingStyle,
    Long coverAssetId,
    Long homeStudioId
) {}
