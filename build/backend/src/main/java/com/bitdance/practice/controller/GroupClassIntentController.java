package com.bitdance.practice.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.practice.dto.CreateGroupClassIntentRequest;
import com.bitdance.practice.dto.GroupClassIntentDto;
import com.bitdance.practice.service.GroupClassIntentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class GroupClassIntentController {

    private final GroupClassIntentService service;

    public GroupClassIntentController(GroupClassIntentService service) {
        this.service = service;
    }

    @PostMapping("/h5/group-class-intents")
    public ApiResponse<GroupClassIntentDto> create(@Valid @RequestBody CreateGroupClassIntentRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @GetMapping("/public/group-class-intents")
    public ApiResponse<List<GroupClassIntentDto>> publicList(
        @RequestParam(required = false) Long studioId,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(defaultValue = "20") int limit
    ) {
        Long currentUserId;
        try {
            currentUserId = CurrentUser.getId();
        } catch (RuntimeException ignored) {
            currentUserId = null;
        }
        return ApiResponse.ok(service.publicList(currentUserId, studioId, danceStyleId, limit));
    }

    @GetMapping("/h5/group-class-intents/mine")
    public ApiResponse<List<GroupClassIntentDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }

    @PostMapping("/h5/group-class-intents/{id}/join")
    public ApiResponse<GroupClassIntentDto> join(@PathVariable Long id) {
        return ApiResponse.ok(service.join(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/group-class-intents/{id}/cancel")
    public ApiResponse<GroupClassIntentDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(CurrentUser.getId(), id));
    }
}
