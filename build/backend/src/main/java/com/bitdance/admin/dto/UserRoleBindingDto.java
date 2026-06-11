package com.bitdance.admin.dto;

public record UserRoleBindingDto(
    Long id,
    Long userId,
    String role,
    String status
) {}
