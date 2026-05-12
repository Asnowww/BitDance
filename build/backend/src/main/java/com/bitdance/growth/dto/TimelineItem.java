package com.bitdance.growth.dto;

import java.time.OffsetDateTime;

public record TimelineItem(
    String type,
    Long refId,
    String title,
    String subtitle,
    OffsetDateTime ts
) {}
