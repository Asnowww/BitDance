package com.bitdance.profile.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSocialAccountRequest(@NotNull Boolean isPublic) {
}
