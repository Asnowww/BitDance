package com.bitdance.iam.dto;

import java.util.List;

public record UserSummary(
    Long id,
    String phone,
    String nickname,
    String avatar,
    List<String> roles
) {}
