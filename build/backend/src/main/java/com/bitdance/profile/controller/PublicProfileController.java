package com.bitdance.profile.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.profile.dto.PublicUserProfileDto;
import com.bitdance.profile.dto.PublicUserSearchResponse;
import com.bitdance.profile.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/public/users/search")
    public ApiResponse<PublicUserSearchResponse> search(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(profileService.searchPublicUsers(q, page, pageSize, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/public/users/{userId}/profile")
    public ApiResponse<PublicUserProfileDto> detail(@PathVariable Long userId) {
        return ApiResponse.ok(profileService.getPublicProfile(userId, CurrentUser.getIdOrNull()));
    }
}
