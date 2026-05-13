package com.bitdance.booking.dto;

import jakarta.validation.constraints.Size;

public record RejectBookingRequest(@Size(max = 500) String reason) {}
