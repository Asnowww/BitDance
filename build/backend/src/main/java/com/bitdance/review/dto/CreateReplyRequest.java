package com.bitdance.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReplyRequest(
    @NotNull Long reviewId,
    @NotBlank @Size(min = 1, max = 1000) String replyContent,
    Boolean isOfficial
) {}
