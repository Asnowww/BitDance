package com.bitdance.iam.dto;

import jakarta.validation.constraints.NotBlank;

public record WechatLoginRequest(@NotBlank String code) {
}
