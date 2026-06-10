package com.bitdance.coachops.controller;

import com.bitdance.coachops.dto.CertificationDto;
import com.bitdance.coachops.dto.HandleCertificationRequest;
import com.bitdance.coachops.dto.SubmitCertificationRequest;
import com.bitdance.coachops.service.CoachCertificationService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
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
public class CoachCertificationController {

    private final CoachCertificationService service;

    public CoachCertificationController(CoachCertificationService service) {
        this.service = service;
    }

    @PostMapping("/h5/coach/certifications")
    public ApiResponse<CertificationDto> submit(@Valid @RequestBody SubmitCertificationRequest body) {
        return ApiResponse.ok(service.submit(CurrentUser.getId(), body));
    }

    @GetMapping("/h5/coach/certifications/mine")
    public ApiResponse<List<CertificationDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }

    @GetMapping("/admin/coach-certifications")
    public ApiResponse<Page<CertificationDto>> listByStatus(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.listByStatus(status, page, pageSize));
    }

    @GetMapping("/h5/coach/platform/coach-certifications")
    public ApiResponse<Page<CertificationDto>> platformListByStatus(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.listByStatus(status, page, pageSize));
    }

    @PostMapping("/admin/coach-certifications/{id}/approve")
    public ApiResponse<CertificationDto> approve(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCertificationRequest body
    ) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id, body));
    }

    @PostMapping("/h5/coach/platform/coach-certifications/{id}/approve")
    public ApiResponse<CertificationDto> platformApprove(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCertificationRequest body
    ) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id, body));
    }

    @PostMapping("/admin/coach-certifications/{id}/reject")
    public ApiResponse<CertificationDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCertificationRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }

    @PostMapping("/h5/coach/platform/coach-certifications/{id}/reject")
    public ApiResponse<CertificationDto> platformReject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCertificationRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }
}
