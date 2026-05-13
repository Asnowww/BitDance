package com.bitdance.merchant.dto;

import jakarta.validation.constraints.Size;

public record HandleClaimRequest(@Size(max = 1000) String remark) {}
