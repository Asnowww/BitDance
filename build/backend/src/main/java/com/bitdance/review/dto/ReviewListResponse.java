package com.bitdance.review.dto;

import java.util.List;

public record ReviewListResponse(
    List<ReviewDto> list,
    int page,
    int pageSize,
    long total
) {}
