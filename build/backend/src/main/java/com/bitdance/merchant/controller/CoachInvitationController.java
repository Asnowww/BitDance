package com.bitdance.merchant.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.merchant.dto.StudioCoachRelationDto;
import com.bitdance.merchant.service.CoachRelationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CoachInvitationController {

    private final CoachRelationService service;

    public CoachInvitationController(CoachRelationService service) {
        this.service = service;
    }

    @GetMapping("/h5/coach/invitations")
    public ApiResponse<List<StudioCoachRelationDto>> invitations() {
        return ApiResponse.ok(service.myInvitations(CurrentUser.getId()));
    }

    @PostMapping("/h5/coach/invitations/{id}/accept")
    public ApiResponse<StudioCoachRelationDto> accept(@PathVariable Long id) {
        return ApiResponse.ok(service.accept(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/coach/invitations/{id}/reject")
    public ApiResponse<StudioCoachRelationDto> reject(@PathVariable Long id) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id));
    }
}
