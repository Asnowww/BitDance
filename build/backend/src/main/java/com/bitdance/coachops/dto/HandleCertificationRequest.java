package com.bitdance.coachops.dto;

import jakarta.validation.constraints.Size;

public record HandleCertificationRequest(@Size(max = 1000) String remark) {}
