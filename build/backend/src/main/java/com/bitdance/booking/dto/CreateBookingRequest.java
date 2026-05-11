package com.bitdance.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
    @NotNull Long courseId,
    Long courseScheduleId,
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String contactPhone,
    @Size(max = 500) String bookingNote
) {}
