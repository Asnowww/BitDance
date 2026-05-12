package com.bitdance.buddy.controller;

import com.bitdance.buddy.dto.BuddyDto;
import com.bitdance.buddy.dto.CreateRatingRequest;
import com.bitdance.buddy.dto.RatingDto;
import com.bitdance.buddy.service.BuddyService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class BuddyController {

    private final BuddyService service;

    public BuddyController(BuddyService service) {
        this.service = service;
    }

    @PostMapping("/h5/practices/{postId}/ratings")
    public ApiResponse<RatingDto> rate(
        @PathVariable Long postId,
        @Valid @RequestBody CreateRatingRequest body
    ) {
        return ApiResponse.ok(service.rate(CurrentUser.getId(), postId, body));
    }

    @GetMapping("/h5/practices/{postId}/ratings")
    public ApiResponse<List<RatingDto>> ratings(@PathVariable Long postId) {
        return ApiResponse.ok(service.ratingsOfPost(CurrentUser.getId(), postId));
    }

    @GetMapping("/h5/buddies")
    public ApiResponse<List<BuddyDto>> myBuddies(
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.listMyBuddies(CurrentUser.getId(), status));
    }

    @PostMapping("/h5/buddies/{userId}/block")
    public ApiResponse<BuddyDto> block(@PathVariable Long userId) {
        return ApiResponse.ok(service.block(CurrentUser.getId(), userId));
    }

    @DeleteMapping("/h5/buddies/{userId}")
    public ApiResponse<BuddyDto> remove(@PathVariable Long userId) {
        return ApiResponse.ok(service.removeBuddy(CurrentUser.getId(), userId));
    }
}
