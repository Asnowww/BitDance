package com.bitdance.coachops.dto;

import java.math.BigDecimal;

public record CoachDashboardDto(
    BigDecimal monthIncome,
    long monthOrderCount,
    long checkinCount,
    long refundCount,
    long courseBookingCount,
    long workshopSignupCount,
    long pendingReviewReplies,
    BigDecimal avgRating,
    long monthSessions,
    long monthWorkshopOrders,
    long ratingCount
) {
    public CoachDashboardDto(
        long monthSessions,
        long monthWorkshopOrders,
        BigDecimal monthIncome,
        long pendingReviewReplies,
        BigDecimal avgRating,
        long ratingCount
    ) {
        this(
            monthIncome,
            monthWorkshopOrders,
            0,
            0,
            0,
            monthWorkshopOrders,
            pendingReviewReplies,
            avgRating,
            monthSessions,
            monthWorkshopOrders,
            ratingCount
        );
    }
}
