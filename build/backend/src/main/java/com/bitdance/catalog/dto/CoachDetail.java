package com.bitdance.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record CoachDetail(
    Long id,
    Long userId,
    String displayName,
    String intro,
    String teachingStyle,
    String availableTimeSlots,
    String certificationStatus,
    Long homeStudioId,
    Long coverAssetId,
    BigDecimal avgRating,
    List<CoachStyleDto> styles,
    boolean favored
) {}
