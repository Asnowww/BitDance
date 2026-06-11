package com.bitdance.profile.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.profile.dto.SocialAccountDto;
import com.bitdance.profile.dto.UpdateSocialAccountRequest;
import com.bitdance.profile.service.ProfileService;
import com.bitdance.profile.service.SocialAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class SocialAccountController {

    private final SocialAccountService service;
    private final ProfileService profileService;

    public SocialAccountController(SocialAccountService service, ProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    @GetMapping("/h5/social-accounts")
    public ApiResponse<List<SocialAccountDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }

    @PutMapping("/h5/social-accounts/{id}")
    public ApiResponse<SocialAccountDto> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateSocialAccountRequest body
    ) {
        return ApiResponse.ok(service.updateMine(CurrentUser.getId(), id, body));
    }

    @GetMapping("/public/users/{userId}/social-accounts")
    public ApiResponse<List<SocialAccountDto>> publicAccounts(@PathVariable Long userId) {
        if (!profileService.canViewProfile(userId, CurrentUser.getIdOrNull())) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(service.publicAccounts(userId));
    }
}
