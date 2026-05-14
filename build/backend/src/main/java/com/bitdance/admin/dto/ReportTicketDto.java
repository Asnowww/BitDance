package com.bitdance.admin.dto;

import java.time.OffsetDateTime;

public record ReportTicketDto(
    Long id,
    Long reporterUserId,
    String targetType,
    Long targetId,
    String reasonCode,
    String reasonDetail,
    String reportStatus,
    Long handledByUserId,
    OffsetDateTime handledAt,
    String handleResult,
    OffsetDateTime createdAt
) {}
