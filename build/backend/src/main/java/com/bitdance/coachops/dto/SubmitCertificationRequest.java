package com.bitdance.coachops.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitCertificationRequest(
    @Pattern(regexp = "independent|studio_affiliated") String applicationType,
    @Pattern(regexp = "full_time|signed|freelance") String coachType,
    @Size(max = 2000) String remark
) {
    public SubmitCertificationRequest(String applicationType, String remark) {
        this(applicationType, "freelance", remark);
    }
}
