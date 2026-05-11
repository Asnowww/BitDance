package com.bitdance.practice.dto;

import jakarta.validation.constraints.Size;

public record JoinPracticeRequest(
    @Size(max = 500) String message
) {}
