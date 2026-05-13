package com.bitdance.review.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.review.dto.CreateReplyRequest;
import com.bitdance.review.dto.ReviewReplyDto;
import com.bitdance.review.service.ReviewReplyService;
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
import java.util.Map;

@RestController
@RequestMapping
public class ReviewReplyController {

    private final ReviewReplyService service;

    public ReviewReplyController(ReviewReplyService service) {
        this.service = service;
    }

    @PostMapping("/h5/review-replies")
    public ApiResponse<ReviewReplyDto> create(@Valid @RequestBody CreateReplyRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @DeleteMapping("/h5/review-replies/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        service.delete(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @GetMapping("/public/review-replies")
    public ApiResponse<List<ReviewReplyDto>> listByReview(@RequestParam Long reviewId) {
        return ApiResponse.ok(service.listByReview(reviewId));
    }

    @GetMapping("/h5/review-replies/mine")
    public ApiResponse<List<ReviewReplyDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }
}
