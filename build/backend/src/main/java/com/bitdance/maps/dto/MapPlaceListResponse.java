package com.bitdance.maps.dto;

import java.util.List;

// M1 腾讯地图地点检索分页响应：保持前端列表式选择和后端候选搜索解耦。
public record MapPlaceListResponse(
    List<MapPlaceResult> list,
    int page,
    int pageSize,
    Integer total
) {}
