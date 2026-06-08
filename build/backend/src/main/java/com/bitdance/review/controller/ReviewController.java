package com.bitdance.review.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.review.dto.CreateReviewRequest;
import com.bitdance.review.dto.ReviewDto;
import com.bitdance.review.dto.ReviewListResponse;
import com.bitdance.review.dto.ReviewSummary;
import com.bitdance.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
@RequestMapping
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/h5/reviews")
    public ApiResponse<ReviewDto> create(@Valid @RequestBody CreateReviewRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @DeleteMapping("/h5/reviews/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        service.delete(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @GetMapping("/public/reviews")
    public ApiResponse<ReviewListResponse> list(
        @RequestParam @NotBlank String targetType,
        @RequestParam @NotNull Long targetId,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") @Min(1) @Max(100) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ApiResponse.ok(service.list(targetType, targetId, sort, status, page, pageSize));
    }

    @GetMapping("/public/reviews/summary")
    public ApiResponse<ReviewSummary> summary(
        @RequestParam @NotBlank String targetType,
        @RequestParam @NotNull Long targetId
    ) {
        return ApiResponse.ok(service.summary(targetType, targetId));
    }

    @GetMapping("/public/users/{userId}/reviews")
    public ApiResponse<ReviewListResponse> listByUser(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "1") @Min(1) @Max(100) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ApiResponse.ok(service.listByUser(userId, page, pageSize));
    }
}
