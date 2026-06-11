package com.bitdance.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record GrantUserRoleRequest(
    @NotBlank String role
) {}
