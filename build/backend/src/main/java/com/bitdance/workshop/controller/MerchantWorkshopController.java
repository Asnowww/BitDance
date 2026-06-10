package com.bitdance.workshop.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.workshop.dto.CreateSessionRequest;
import com.bitdance.workshop.dto.CreateWorkshopRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.service.MerchantWorkshopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @PostMapping("/workshops/{id}/approve")
    public ApiResponse<WorkshopDetail> approve(@PathVariable Long id) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id));
    }

    @PostMapping("/workshops/{id}/reject")
    public ApiResponse<WorkshopDetail> reject(@PathVariable Long id) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id));
    }

    @PostMapping("/workshop-sessions")
    public ApiResponse<SessionDto> addSession(@Valid @RequestBody CreateSessionRequest body) {
        return ApiResponse.ok(service.addSession(CurrentUser.getId(), body));
    }

    @GetMapping("/workshops")
    public ApiResponse<List<WorkshopDetail>> list(@RequestParam Long studioId) {
        return ApiResponse.ok(service.list(CurrentUser.getId(), studioId));
    }

    @GetMapping("/workshop-orders")
    public ApiResponse<List<OrderDto>> orders(
        @RequestParam Long studioId,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.orders(CurrentUser.getId(), studioId, status));
    }

    @GetMapping("/workshop-checkins/history")
    public ApiResponse<List<OrderDto>> history(@RequestParam Long studioId) {
        return ApiResponse.ok(service.orders(CurrentUser.getId(), studioId, "completed"));
    }
}
