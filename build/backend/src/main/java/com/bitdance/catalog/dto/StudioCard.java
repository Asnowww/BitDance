package com.bitdance.catalog.dto;

import java.math.BigDecimal;

public record StudioCard(
    Long id,
    String name,
    String address,
    Long cityId,
    Long businessDistrictId,
    Long coverAssetId,
    BigDecimal distanceKm,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean favored
) {}
