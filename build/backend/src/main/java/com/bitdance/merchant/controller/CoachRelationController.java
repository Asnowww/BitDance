package com.bitdance.merchant.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.merchant.dto.InviteCoachRequest;
import com.bitdance.merchant.dto.StudioCoachRelationDto;
import com.bitdance.merchant.dto.UpdateCoachRelationRequest;
import com.bitdance.merchant.service.CoachRelationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/merchant/coach-relations")
public class CoachRelationController {

    private final CoachRelationService service;

    public CoachRelationController(CoachRelationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<StudioCoachRelationDto> invite(@Valid @RequestBody InviteCoachRequest body) {
        return ApiResponse.ok(service.invite(CurrentUser.getId(), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudioCoachRelationDto> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCoachRelationRequest body
    ) {
        return ApiResponse.ok(service.update(CurrentUser.getId(), id, body));
    }

    @GetMapping
    public ApiResponse<List<StudioCoachRelationDto>> listByStudio(@RequestParam Long studioId) {
        return ApiResponse.ok(service.listByStudio(CurrentUser.getId(), studioId));
    }
}
