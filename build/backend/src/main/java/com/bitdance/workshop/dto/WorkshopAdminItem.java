package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WorkshopAdminItem(
    Long id,
    Long studioId,
    Long coachId,
    Long cityId,
    String workshopName,
    BigDecimal priceAmount,
    OffsetDateTime signupDeadline,
    String auditStatus,
    String publishStatus
) {}
