package com.bitdance.practice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PracticePostDto(
    Long id,
    Long creatorUserId,
    Long danceStyleId,
    Long studioId,
    Long cityId,
    String locationName,
    String locationAddress,
    BigDecimal longitude,
    BigDecimal latitude,
    String skillLevel,
    Integer expectedPeopleMin,
    Integer expectedPeopleMax,
    Integer currentPeopleCount,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    OffsetDateTime expiresAt,
    String postStatus,
    String description,
    OffsetDateTime createdAt,
    Long distanceMeters,
    List<PracticeParticipantDto> participants,
    Boolean completionConfirmedByMe,
    Boolean allCompletedConfirmed,
    List<PracticeParticipantDto> ratingTargets,
    List<Long> ratedUserIds
) {
    public PracticePostDto(
        Long id,
        Long creatorUserId,
        Long danceStyleId,
        Long studioId,
        Long cityId,
        String locationName,
        String locationAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        String skillLevel,
        Integer expectedPeopleMin,
        Integer expectedPeopleMax,
        Integer currentPeopleCount,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime expiresAt,
        String postStatus,
        String description,
        OffsetDateTime createdAt,
        Long distanceMeters
    ) {
        this(
            id, creatorUserId, danceStyleId, studioId, cityId, locationName, locationAddress,
            longitude, latitude, skillLevel, expectedPeopleMin, expectedPeopleMax,
            currentPeopleCount, startAt, endAt, expiresAt, postStatus, description,
            createdAt, distanceMeters, List.of(), Boolean.FALSE, Boolean.FALSE, List.of(), List.of()
        );
    }
}
