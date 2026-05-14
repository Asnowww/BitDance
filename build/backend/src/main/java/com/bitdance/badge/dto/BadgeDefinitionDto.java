package com.bitdance.badge.dto;

public record BadgeDefinitionDto(
    Long id,
    String badgeCode,
    String badgeName,
    String description,
    Long iconAssetId,
    String ruleType,
    String ruleConfig,
    String status
) {}
