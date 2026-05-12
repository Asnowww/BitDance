package com.bitdance.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReportRequest(
    @NotBlank @Pattern(regexp = "spam|adult|violence|fraud|other") String reasonCode,
    @Size(max = 2000) String reasonDetail
) {}
