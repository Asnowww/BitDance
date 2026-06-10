package com.bitdance.maps.dto;

import java.math.BigDecimal;

// M1 腾讯地图地点候选：用于管理端手动标注前核对舞室真实点位。
public record MapPlaceResult(
    String id,
    String title,
    String address,
    String category,
    BigDecimal latitude,
    BigDecimal longitude,
    String tel,
    String adcode
) {}
