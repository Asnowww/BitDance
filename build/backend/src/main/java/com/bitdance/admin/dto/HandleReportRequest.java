package com.bitdance.admin.dto;

import jakarta.validation.constraints.Size;

public record HandleReportRequest(@Size(max = 1000) String handleResult) {}
