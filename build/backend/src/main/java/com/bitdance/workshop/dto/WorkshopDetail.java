package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record WorkshopDetail(
    Long id,
    Long studioId,
    Long coachId,
    Long cityId,
    Long danceStyleId,
    String workshopName,
    Long coverAssetId,
    String intro,
    String address,
    String locationName,
    BigDecimal priceAmount,
    Integer minPeople,
    Integer maxPeople,
    OffsetDateTime signupDeadline,
    String publishStatus,
    String auditStatus,
    List<SessionDto> sessions,
    boolean favored
) {}
