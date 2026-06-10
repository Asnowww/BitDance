package com.bitdance.review.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReviewMediaDto(
    Long assetId,
    @Pattern(regexp = "image|video", message = "type 必须是 image/video")
    String type,
    @Size(max = 2048)
    String url,
    @Size(max = 255)
    String name,
    @Min(0)
    Long size
) {}
