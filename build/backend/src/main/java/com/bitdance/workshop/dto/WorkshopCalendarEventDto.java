package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WorkshopCalendarEventDto(
    Long orderId,
    Long workshopId,
    Long sessionId,
    String workshopName,
    String coachName,
    String locationName,
    String address,
    String orderStatus,
    BigDecimal amountPaid,
    String checkinCode,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    String reminderStage,
    String reminderTitle,
    String reminderBody,
    boolean allowCheckin
) {}
