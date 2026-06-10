package com.bitdance.courseorder.dto;

import jakarta.validation.constraints.Size;

public record HandleCourseRefundRequest(
    @Size(max = 1000) String remark
) {}
