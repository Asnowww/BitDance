package com.bitdance.message.dto;

import java.time.OffsetDateTime;

public record NotificationDto(
    Long id,
    String noticeType,
    String category,
    String title,
    String content,
    String targetType,
    Long targetId,
    Boolean isRead,
    OffsetDateTime readAt,
    OffsetDateTime createdAt
) {}
