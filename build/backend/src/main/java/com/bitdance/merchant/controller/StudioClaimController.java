package com.bitdance.merchant.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.merchant.dto.HandleClaimRequest;
import com.bitdance.merchant.dto.StudioClaimDto;
import com.bitdance.merchant.dto.SubmitClaimRequest;
import com.bitdance.merchant.service.StudioClaimService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
public class StudioClaimController {

    private final StudioClaimService service;

    public StudioClaimController(StudioClaimService service) {
        this.service = service;
    }

    @PostMapping("/h5/studio-claims")
    public ApiResponse<StudioClaimDto> submit(@Valid @RequestBody SubmitClaimRequest body) {
        return ApiResponse.ok(service.submit(CurrentUser.getId(), body));
    }

    @GetMapping("/h5/studio-claims/mine")
    public ApiResponse<List<StudioClaimDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }

    @GetMapping("/admin/studio-claims")
    public ApiResponse<Page<StudioClaimDto>> listByStatus(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.listByStatus(status, page, pageSize));
    }

    @PostMapping("/admin/studio-claims/{id}/approve")
    public ApiResponse<StudioClaimDto> approve(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleClaimRequest body
    ) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id, body));
    }

    @PostMapping("/admin/studio-claims/{id}/reject")
    public ApiResponse<StudioClaimDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleClaimRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }
}
