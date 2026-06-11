package com.bitdance.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreatePostRequest(
    @Pattern(regexp = "note|video|experience|practice") String postType,
    @NotBlank @Size(min = 1, max = 5000) String contentText,
    Long danceStyleId,
    Long relatedCourseId,
    Long relatedWorkshopId,
    Long cityId,
    @Size(max = 200) String locationName,
    BigDecimal longitude,
    BigDecimal latitude,
    @Pattern(regexp = "public|followers|private") String visibility,
    @Size(max = 5) List<String> topicNames,
    @Size(max = 10) List<Long> mediaAssetIds
) {}
