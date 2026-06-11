package com.bitdance.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateReviewRequest(
    @NotBlank @Pattern(regexp = "studio|course|coach|workshop", message = "targetType 必须是 studio/course/coach/workshop")
    String targetType,
    @NotNull Long targetId,
    @NotNull @DecimalMin("1.00") @DecimalMax("5.00") BigDecimal overallScore,
    @Size(max = 5000) String contentText,
    @NotEmpty @Valid List<DimensionScoreDto> dimensions,
    @Pattern(regexp = "trial|order|checkin", message = "sourceType 必须是 trial/order/checkin")
    String sourceType,
    Long sourceRefId
) {}
