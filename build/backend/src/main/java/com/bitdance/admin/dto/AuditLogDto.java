package com.bitdance.admin.dto;

import java.time.OffsetDateTime;

public record AuditLogDto(
    Long id,
    Long actorUserId,
    String actorRoleCode,
    String actionCode,
    String targetType,
    Long targetId,
    OffsetDateTime createdAt
) {}
