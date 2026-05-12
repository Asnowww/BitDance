package com.bitdance.growth.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.growth.dto.BadgeDto;
import com.bitdance.growth.dto.CheckinDto;
import com.bitdance.growth.dto.CreateCheckinRequest;
import com.bitdance.growth.dto.CreateWorkRequest;
import com.bitdance.growth.dto.GoalDto;
import com.bitdance.growth.dto.GrowthStats;
import com.bitdance.growth.dto.TimelineItem;
import com.bitdance.growth.dto.UpsertGoalRequest;
import com.bitdance.growth.dto.WorkDto;
import com.bitdance.growth.service.GrowthService;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/h5/growth")
public class GrowthController {

    private final GrowthService service;

    public GrowthController(GrowthService service) {
        this.service = service;
    }

    @PostMapping("/checkins")
    public ApiResponse<CheckinDto> createCheckin(@Valid @RequestBody CreateCheckinRequest body) {
        return ApiResponse.ok(service.createCheckin(CurrentUser.getId(), body));
    }

    @GetMapping("/checkins")
    public ApiResponse<List<CheckinDto>> listCheckins() {
        return ApiResponse.ok(service.listCheckins(CurrentUser.getId()));
    }

    @DeleteMapping("/checkins/{id}")
    public ApiResponse<Map<String, Object>> deleteCheckin(@PathVariable Long id) {
        service.deleteCheckin(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @GetMapping("/stats")
    public ApiResponse<GrowthStats> stats() {
        return ApiResponse.ok(service.stats(CurrentUser.getId()));
    }

    @GetMapping("/timeline")
    public ApiResponse<List<TimelineItem>> timeline() {
        return ApiResponse.ok(service.timeline(CurrentUser.getId()));
    }

    @PutMapping("/goals/active")
    public ApiResponse<GoalDto> upsertGoal(@Valid @RequestBody UpsertGoalRequest body) {
        return ApiResponse.ok(service.upsertActiveGoal(CurrentUser.getId(), body));
    }

    @GetMapping("/goals/active")
    public ApiResponse<GoalDto> activeGoal() {
        return ApiResponse.ok(service.activeGoal(CurrentUser.getId()));
    }

    @PostMapping("/works")
    public ApiResponse<WorkDto> createWork(@Valid @RequestBody CreateWorkRequest body) {
        return ApiResponse.ok(service.createWork(CurrentUser.getId(), body));
    }

    @GetMapping("/works")
    public ApiResponse<List<WorkDto>> listWorks() {
        return ApiResponse.ok(service.listWorks(CurrentUser.getId()));
    }

    @DeleteMapping("/works/{id}")
    public ApiResponse<Map<String, Object>> deleteWork(@PathVariable Long id) {
        service.deleteWork(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @GetMapping("/badges")
    public ApiResponse<List<BadgeDto>> badges() {
        return ApiResponse.ok(service.listBadges(CurrentUser.getId()));
    }
}
