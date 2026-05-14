package com.bitdance.coachops.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitCertificationRequest(
    @Pattern(regexp = "independent|studio_affiliated") String applicationType,
    @Size(max = 2000) String remark
) {}
