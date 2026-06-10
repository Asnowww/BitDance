package com.bitdance.coachops.controller;

import com.bitdance.coachops.dto.CoachDashboardDto;
import com.bitdance.coachops.dto.CoachMeDto;
import com.bitdance.coachops.dto.UpdateCoachProfileRequest;
import com.bitdance.coachops.service.CoachOpsService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/h5/coach")
public class CoachOpsController {

    private final CoachOpsService service;

    public CoachOpsController(CoachOpsService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ApiResponse<CoachMeDto> me() {
        return ApiResponse.ok(service.me(CurrentUser.getId()));
    }

    @PutMapping("/me/profile")
    public ApiResponse<CoachMeDto> updateProfile(@Valid @RequestBody UpdateCoachProfileRequest body) {
        return ApiResponse.ok(service.updateProfile(CurrentUser.getId(), body));
    }

    @GetMapping("/dashboard")
    public ApiResponse<CoachDashboardDto> dashboard(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) Long studioId
    ) {
        if (role == null && studioId == null) {
            return ApiResponse.ok(service.dashboard(CurrentUser.getId()));
        }
        return ApiResponse.ok(service.dashboard(CurrentUser.getId(), role, studioId));
    }

    @GetMapping("/courses")
    public ApiResponse<List<CoachOpsService.CourseSummaryDto>> courses() {
        return ApiResponse.ok(service.myCourses(CurrentUser.getId()));
    }
}
