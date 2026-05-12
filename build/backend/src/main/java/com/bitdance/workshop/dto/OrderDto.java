package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderDto(
    Long id,
    String orderNo,
    Long workshopId,
    Long workshopSessionId,
    Long userId,
    BigDecimal amountPayable,
    BigDecimal amountPaid,
    String orderStatus,
    String paymentTxnNo,
    String checkinCode,
    OffsetDateTime paidAt,
    OffsetDateTime canceledAt,
    OffsetDateTime refundedAt,
    OffsetDateTime createdAt
) {}
