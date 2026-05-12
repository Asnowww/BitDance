package com.bitdance.workshop.dto;

import jakarta.validation.constraints.Size;

public record RefundRequest(@Size(max = 500) String reason) {}
