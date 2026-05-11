package com.bitdance.catalog.controller;

import com.bitdance.catalog.dto.StudioDetail;
import com.bitdance.catalog.dto.StudioListResponse;
import com.bitdance.catalog.service.StudioService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/studios")
public class StudioController {

    private final StudioService studioService;

    public StudioController(StudioService studioService) {
        this.studioService = studioService;
    }

    @GetMapping("/nearby")
    public ApiResponse<StudioListResponse> nearby(
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        @RequestParam(required = false) Double distanceKm,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(studioService.searchNearby(
            cityId, latitude, longitude, distanceKm, keyword, danceStyleId,
            page, pageSize, CurrentUser.getIdOrNull()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudioDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(studioService.detail(id, CurrentUser.getIdOrNull()));
    }
}
