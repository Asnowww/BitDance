package com.bitdance.workshop.dto;

import java.util.List;

public record WorkshopListResponse(
    List<WorkshopBrief> list,
    int page,
    int pageSize,
    long total
) {}
