package com.bitdance.courseorder.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.courseorder.dto.CheckinCourseOrderRequest;
import com.bitdance.courseorder.dto.CourseOrderDto;
import com.bitdance.courseorder.dto.CourseRefundDto;
import com.bitdance.courseorder.dto.HandleCourseRefundRequest;
import com.bitdance.courseorder.service.CourseOrderService;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MerchantCourseOrderController {

    private final CourseOrderService service;

    public MerchantCourseOrderController(CourseOrderService service) {
        this.service = service;
    }

    @GetMapping("/merchant/course-orders")
    public ApiResponse<List<CourseOrderDto>> orders(
        @RequestParam Long studioId,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.merchantOrders(CurrentUser.getId(), studioId, status));
    }

    @PostMapping("/merchant/course-orders/{id}/checkin")
    public ApiResponse<CourseOrderDto> checkin(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) CheckinCourseOrderRequest body
    ) {
        return ApiResponse.ok(service.merchantCheckin(CurrentUser.getId(), id, body));
    }

    @PostMapping("/merchant/course-orders/checkin-by-code")
    public ApiResponse<CourseOrderDto> checkinByCode(
        @RequestParam @Pattern(regexp = "^[0-9]{8}$") String code
    ) {
        return ApiResponse.ok(service.merchantCheckinByCode(CurrentUser.getId(), code));
    }

    @GetMapping("/merchant/course-checkins/history")
    public ApiResponse<List<CourseOrderDto>> history(
        @RequestParam Long studioId
    ) {
        return ApiResponse.ok(service.merchantOrders(CurrentUser.getId(), studioId, "checked_in"));
    }

    @GetMapping("/merchant/course-refunds")
    public ApiResponse<List<CourseRefundDto>> refunds(
        @RequestParam Long studioId,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.refunds(CurrentUser.getId(), studioId, status));
    }

    @PostMapping("/merchant/course-refunds/{id}/approve")
    public ApiResponse<CourseRefundDto> approve(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCourseRefundRequest body
    ) {
        return ApiResponse.ok(service.approveRefund(CurrentUser.getId(), id, body));
    }

    @PostMapping("/merchant/course-refunds/{id}/reject")
    public ApiResponse<CourseRefundDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleCourseRefundRequest body
    ) {
        return ApiResponse.ok(service.rejectRefund(CurrentUser.getId(), id, body));
    }
}
