package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WorkshopBrief(
    Long id,
    Long studioId,
    Long coachId,
    Long cityId,
    Long danceStyleId,
    String workshopName,
    Long coverAssetId,
    String locationName,
    BigDecimal priceAmount,
    OffsetDateTime signupDeadline,
    String publishStatus
) {}
