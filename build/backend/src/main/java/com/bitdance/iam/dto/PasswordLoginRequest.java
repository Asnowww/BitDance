package com.bitdance.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordLoginRequest(
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
    @NotBlank @Size(min = 6, max = 32) String password
) {}
