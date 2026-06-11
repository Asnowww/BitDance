package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WorkshopReviewSnippet(
    Long id,
    Long userId,
    String authorName,
    BigDecimal rating,
    String text,
    Boolean verified,
    OffsetDateTime publishedAt
) {}
