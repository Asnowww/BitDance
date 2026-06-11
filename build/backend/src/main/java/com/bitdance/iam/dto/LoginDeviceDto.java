package com.bitdance.iam.dto;

import java.time.OffsetDateTime;

public record LoginDeviceDto(
    Long id,
    String deviceName,
    String platform,
    String ipAddress,
    OffsetDateTime lastLoginAt,
    Boolean isCurrent,
    Boolean isTrusted
) {
}
