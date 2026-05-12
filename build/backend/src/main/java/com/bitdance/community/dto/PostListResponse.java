package com.bitdance.community.dto;

import java.util.List;

public record PostListResponse(
    List<PostDto> list,
    int page,
    int pageSize,
    long total
) {}
