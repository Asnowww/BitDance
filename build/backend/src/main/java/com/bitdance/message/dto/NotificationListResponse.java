package com.bitdance.message.dto;

import java.util.List;

public record NotificationListResponse(
    List<NotificationDto> list,
    int page,
    int pageSize,
    long total,
    long unread
) {}
