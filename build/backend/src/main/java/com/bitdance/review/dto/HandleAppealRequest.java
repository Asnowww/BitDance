package com.bitdance.review.dto;

import jakarta.validation.constraints.Size;

public record HandleAppealRequest(@Size(max = 1000) String remark) {}
