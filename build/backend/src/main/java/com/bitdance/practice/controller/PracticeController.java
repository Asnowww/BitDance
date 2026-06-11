package com.bitdance.practice.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.practice.dto.CreatePracticeRequest;
import com.bitdance.practice.dto.JoinPracticeRequest;
import com.bitdance.practice.dto.JoinRequestDto;
import com.bitdance.practice.dto.PracticeListResponse;
import com.bitdance.practice.dto.PracticePostDto;
import com.bitdance.practice.service.PracticeService;
import com.bitdance.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping
public class PracticeController {

    private final PracticeService service;
    private final ProfileService profileService;

    public PracticeController(PracticeService service, ProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    @PostMapping("/h5/practices")
    public ApiResponse<PracticePostDto> create(@Valid @RequestBody CreatePracticeRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @GetMapping("/public/practices")
    public ApiResponse<PracticeListResponse> square(
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(required = false) String skillLevel,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) String scope,
        @RequestParam(defaultValue = "time") String sort,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.square(
            cityId, danceStyleId, skillLevel, longitude, latitude, scope, sort, page, pageSize
        ));
    }

    @GetMapping("/h5/practices/recommendations")
    public ApiResponse<List<PracticePostDto>> recommendations(
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(required = false) String skillLevel,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(service.recommendations(
            CurrentUser.getId(), cityId, danceStyleId, skillLevel, longitude, latitude, limit
        ));
    }

    @GetMapping("/public/practices/{id}")
    public ApiResponse<PracticePostDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @GetMapping("/h5/practices/{id}")
    public ApiResponse<PracticePostDto> detailForUser(@PathVariable Long id) {
        return ApiResponse.ok(service.detailForUser(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/practices/{id}/complete-confirm")
    public ApiResponse<PracticePostDto> confirmCompleted(@PathVariable Long id) {
        return ApiResponse.ok(service.confirmCompleted(CurrentUser.getId(), id));
    }

    @GetMapping("/public/users/{userId}/practices")
    public ApiResponse<List<PracticePostDto>> publicPostsByUser(@PathVariable Long userId) {
        if (!profileService.canViewPractice(userId, CurrentUser.getIdOrNull())) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(service.publicPostsByCreator(userId));
    }

    @PostMapping("/h5/practices/{id}/cancel")
    public ApiResponse<PracticePostDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/practices/{id}/join")
    public ApiResponse<JoinRequestDto> apply(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) JoinPracticeRequest body
    ) {
        return ApiResponse.ok(service.apply(CurrentUser.getId(), id, body));
    }

    @GetMapping("/h5/practices/{id}/requests")
    public ApiResponse<List<JoinRequestDto>> requestsOfPost(@PathVariable Long id) {
        return ApiResponse.ok(service.requestsOfPost(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/practice-requests/{requestId}/accept")
    public ApiResponse<JoinRequestDto> accept(@PathVariable Long requestId) {
        return ApiResponse.ok(service.accept(CurrentUser.getId(), requestId));
    }

    @PostMapping("/h5/practice-requests/{requestId}/reject")
    public ApiResponse<JoinRequestDto> reject(@PathVariable Long requestId) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), requestId));
    }

    @PostMapping("/h5/practice-requests/{requestId}/cancel")
    public ApiResponse<JoinRequestDto> cancelByApplicant(@PathVariable Long requestId) {
        return ApiResponse.ok(service.cancelByApplicant(CurrentUser.getId(), requestId));
    }

    @GetMapping("/h5/practices/mine")
    public ApiResponse<List<PracticePostDto>> myPosts() {
        return ApiResponse.ok(service.myPosts(CurrentUser.getId()));
    }

    @GetMapping("/h5/practice-requests/mine")
    public ApiResponse<List<JoinRequestDto>> myJoinRequests() {
        return ApiResponse.ok(service.myJoinRequests(CurrentUser.getId()));
    }
}
