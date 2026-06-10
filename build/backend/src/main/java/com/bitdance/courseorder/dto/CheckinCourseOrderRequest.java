package com.bitdance.courseorder.dto;

import jakarta.validation.constraints.Pattern;

public record CheckinCourseOrderRequest(
    @Pattern(regexp = "^[0-9]{8}$", message = "核销码必须是 8 位数字") String code
) {}
