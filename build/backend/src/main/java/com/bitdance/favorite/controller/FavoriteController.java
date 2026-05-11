package com.bitdance.favorite.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.favorite.dto.FavoriteDto;
import com.bitdance.favorite.dto.ToggleFavoriteRequest;
import com.bitdance.favorite.service.FavoriteService;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/h5/favorites")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> toggle(@Valid @RequestBody ToggleFavoriteRequest body) {
        boolean favored = service.toggle(CurrentUser.getId(), body.targetType(), body.targetId());
        return ApiResponse.ok(Map.of("favored", favored));
    }

    @GetMapping
    public ApiResponse<List<FavoriteDto>> list(@RequestParam(required = false) String targetType) {
        return ApiResponse.ok(service.list(CurrentUser.getId(), targetType));
    }

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> check(
        @RequestParam String targetType,
        @RequestParam Long targetId
    ) {
        boolean favored = service.check(CurrentUser.getId(), targetType, targetId);
        return ApiResponse.ok(Map.of("favored", favored));
    }
}
