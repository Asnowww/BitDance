package com.bitdance.workshop.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull Long workshopId,
    @NotNull Long sessionId
) {}
