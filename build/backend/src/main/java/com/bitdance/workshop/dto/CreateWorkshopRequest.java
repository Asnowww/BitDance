package com.bitdance.workshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateWorkshopRequest(
    @NotNull Long studioId,
    Long coachId,
    @NotNull Long cityId,
    Long danceStyleId,
    @NotBlank @Size(max = 200) String workshopName,
    Long coverAssetId,
    @Size(max = 5000) String intro,
    @NotBlank @Size(max = 1000) String address,
    @NotBlank @Size(max = 200) String locationName,
    BigDecimal longitude,
    BigDecimal latitude,
    @NotNull BigDecimal priceAmount,
    @Min(1) @Max(500) Integer minPeople,
    @Min(1) @Max(500) Integer maxPeople,
    OffsetDateTime signupDeadline,
    @Pattern(regexp = "studio|coach") String sourceType
) {}
