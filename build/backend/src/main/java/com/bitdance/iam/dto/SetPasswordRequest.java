package com.bitdance.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequest(
    @NotBlank @Size(min = 6, max = 32) String password
) {}
