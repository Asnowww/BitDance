package com.bitdance.coachops.dto;

import java.math.BigDecimal;

public record CoachDashboardDto(
    long monthSessions,
    long monthWorkshopOrders,
    BigDecimal monthIncome,
    long pendingReviewReplies,
    BigDecimal avgRating,
    long ratingCount
) {}
