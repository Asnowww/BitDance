package com.bitdance.workshop.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.service.MerchantWorkshopCheckinService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant/workshop-orders")
public class MerchantWorkshopCheckinController {

    private final MerchantWorkshopCheckinService service;

    public MerchantWorkshopCheckinController(MerchantWorkshopCheckinService service) {
        this.service = service;
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<OrderDto> checkin(
        @PathVariable Long id,
        @Valid @RequestBody CheckinRequest body
    ) {
        return ApiResponse.ok(service.checkin(CurrentUser.getId(), id, body.code()));
    }
}
