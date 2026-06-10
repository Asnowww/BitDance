package com.bitdance.catalog.dto;

import java.util.List;

public record StudioListResponse(
    List<StudioCard> list,
    int page,
    int pageSize,
    long total
) {}
