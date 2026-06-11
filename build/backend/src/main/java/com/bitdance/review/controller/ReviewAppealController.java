package com.bitdance.review.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.review.dto.CreateAppealRequest;
import com.bitdance.review.dto.HandleAppealRequest;
import com.bitdance.review.dto.ReviewAppealDto;
import com.bitdance.review.service.ReviewAppealService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping
public class ReviewAppealController {

    private final ReviewAppealService service;

    public ReviewAppealController(ReviewAppealService service) {
        this.service = service;
    }

    @PostMapping("/h5/review-appeals")
    public ApiResponse<ReviewAppealDto> create(@Valid @RequestBody CreateAppealRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @GetMapping("/h5/review-appeals/mine")
    public ApiResponse<List<ReviewAppealDto>> mine() {
        return ApiResponse.ok(service.listMine(CurrentUser.getId()));
    }

    @GetMapping("/admin/review-appeals")
    public ApiResponse<Page<ReviewAppealDto>> listByStatus(
        @RequestParam(required = false)
        @Pattern(regexp = "pending|approved|rejected", message = "status 必须是 pending/approved/rejected")
        String status,
        @RequestParam(defaultValue = "1") @Min(1) @Max(100) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ApiResponse.ok(service.listByStatus(status, page, pageSize));
    }

    @GetMapping("/h5/coach/platform/review-appeals")
    public ApiResponse<Page<ReviewAppealDto>> platformListByStatus(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.listByStatus(status, page, pageSize));
    }

    @PostMapping("/admin/review-appeals/{id}/approve")
    public ApiResponse<ReviewAppealDto> approve(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleAppealRequest body
    ) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id, body));
    }

    @PostMapping("/h5/coach/platform/review-appeals/{id}/approve")
    public ApiResponse<ReviewAppealDto> platformApprove(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleAppealRequest body
    ) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id, body));
    }

    @PostMapping("/admin/review-appeals/{id}/reject")
    public ApiResponse<ReviewAppealDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleAppealRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }

    @PostMapping("/h5/coach/platform/review-appeals/{id}/reject")
    public ApiResponse<ReviewAppealDto> platformReject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleAppealRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }
}
