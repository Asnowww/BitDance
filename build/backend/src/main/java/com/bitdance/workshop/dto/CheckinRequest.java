package com.bitdance.workshop.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckinRequest(@NotBlank String code) {}
