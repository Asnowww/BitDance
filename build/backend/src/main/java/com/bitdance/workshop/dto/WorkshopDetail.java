package com.bitdance.workshop.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record WorkshopDetail(
    Long id,
    Long studioId,
    Long coachId,
    Long cityId,
    Long danceStyleId,
    String workshopName,
    Long coverAssetId,
    String intro,
    String address,
    String locationName,
    BigDecimal priceAmount,
    Integer minPeople,
    Integer maxPeople,
    OffsetDateTime signupDeadline,
    String publishStatus,
    String auditStatus,
    String coachName,
    String coachIntro,
    BigDecimal coachRating,
    String studioName,
    String studioAddress,
    String studioTransportInfo,
    BigDecimal longitude,
    BigDecimal latitude,
    Long reviewCount,
    BigDecimal reviewAverage,
    List<WorkshopReviewSnippet> pastReviews,
    List<SessionDto> sessions,
    boolean favored
) {}
