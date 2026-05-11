package com.bitdance.practice.dto;

import java.util.List;

public record PracticeListResponse(
    List<PracticePostDto> list,
    int page,
    int pageSize,
    long total
) {}
