package com.bitdance.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudioDetail(
    Long id,
    String name,
    String brandName,
    String address,
    String transportInfo,
    Long cityId,
    Long businessDistrictId,
    BigDecimal latitude,
    BigDecimal longitude,
    String contactPhone,
    String intro,
    Long coverAssetId,
    String claimStatus,
    List<Long> danceStyleIds,
    boolean favored
) {}
