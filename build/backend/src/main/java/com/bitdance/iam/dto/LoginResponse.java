package com.bitdance.iam.dto;

public record LoginResponse(String token, UserSummary user) {}
