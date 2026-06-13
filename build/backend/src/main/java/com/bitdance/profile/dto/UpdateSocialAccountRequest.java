package com.bitdance.profile.dto;

import jakarta.validation.constraints.Size;

public record UpdateSocialAccountRequest(
    @Size(max = 32) String platform,
    @Size(max = 100) String accountName,
    @Size(max = 512) String profileUrl,
    Boolean isPublic
) {
}
