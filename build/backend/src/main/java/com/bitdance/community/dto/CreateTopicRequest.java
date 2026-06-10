package com.bitdance.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTopicRequest(
    @NotBlank @Size(min = 1, max = 100) String topicName,
    @Size(max = 500) String description
) {}
