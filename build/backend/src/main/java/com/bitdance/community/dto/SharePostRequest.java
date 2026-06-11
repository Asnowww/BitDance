package com.bitdance.community.dto;

import jakarta.validation.constraints.Pattern;

public record SharePostRequest(
    @Pattern(regexp = "wechat|moments|link|copy|system") String channel
) {}
