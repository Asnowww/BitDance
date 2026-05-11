package com.bitdance.iam.dto;

import jakarta.validation.constraints.Pattern;

public record SendSmsRequest(
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone
) {}
