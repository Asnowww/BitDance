package com.bitdance.practice.dto;

public record PracticeParticipantDto(
    Long userId,
    String role,
    Boolean completionConfirmed,
    Boolean ratedByMe
) {}
