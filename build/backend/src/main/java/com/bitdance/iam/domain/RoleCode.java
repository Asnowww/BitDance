package com.bitdance.iam.domain;

import com.bitdance.common.exception.BizException;

import java.util.Arrays;
import java.util.Locale;

public enum RoleCode {
    USER,
    COACH,
    STUDIO_ADMIN,
    PLATFORM_ADMIN;

    public static RoleCode from(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("INVALID_ROLE", "Invalid role");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(role -> role.name().equals(normalized))
            .findFirst()
            .orElseThrow(() -> new BizException("INVALID_ROLE", "Invalid role: " + value));
    }
}
