package com.bitdance.workshop.dto;

import java.math.BigDecimal;

public record MerchantWorkshopOrderRow(
    Long orderId,
    Long workshopId,
    String workshopTitle,
    String buyerName,
    String sessionDate,
    String sessionTime,
    BigDecimal amount,
    String status,
    String checkinCode
) {}
