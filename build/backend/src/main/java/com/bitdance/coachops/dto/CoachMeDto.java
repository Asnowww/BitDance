package com.bitdance.coachops.dto;

import java.math.BigDecimal;
import java.util.List;

public record CoachMeDto(
    boolean certified,
    Long coachId,
    String displayName,
    String intro,
    String teachingStyle,
    String certificationStatus,
    Long homeStudioId,
    Long coverAssetId,
    BigDecimal avgRating,
    List<Long> activeStudioIds
) {}
