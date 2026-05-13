package com.bitdance.workshop.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.workshop.dto.CreateSessionRequest;
import com.bitdance.workshop.dto.CreateWorkshopRequest;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.service.MerchantWorkshopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant")
public class MerchantWorkshopController {

    private final MerchantWorkshopService service;

    public MerchantWorkshopController(MerchantWorkshopService service) {
        this.service = service;
    }

    @PostMapping("/workshops")
    public ApiResponse<WorkshopDetail> create(@Valid @RequestBody CreateWorkshopRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @PostMapping("/workshops/{id}/publish")
    public ApiResponse<WorkshopDetail> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publish(CurrentUser.getId(), id));
    }

    @PostMapping("/workshops/{id}/offline")
    public ApiResponse<WorkshopDetail> offline(@PathVariable Long id) {
        return ApiResponse.ok(service.offline(CurrentUser.getId(), id));
    }

    @PostMapping("/workshop-sessions")
    public ApiResponse<SessionDto> addSession(@Valid @RequestBody CreateSessionRequest body) {
        return ApiResponse.ok(service.addSession(CurrentUser.getId(), body));
    }
}
