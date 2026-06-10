package com.bitdance.courseorder.dto;

import jakarta.validation.constraints.Size;

public record RefundCourseOrderRequest(
    @Size(max = 2000) String reason
) {}
