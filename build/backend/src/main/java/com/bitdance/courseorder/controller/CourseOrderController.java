package com.bitdance.courseorder.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.courseorder.dto.CourseOrderDto;
import com.bitdance.courseorder.dto.CreateCourseOrderRequest;
import com.bitdance.courseorder.dto.RefundCourseOrderRequest;
import com.bitdance.courseorder.service.CourseOrderService;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseOrderController {

    private final CourseOrderService service;

    public CourseOrderController(CourseOrderService service) {
        this.service = service;
    }

    @PostMapping("/h5/course-orders")
    public ApiResponse<CourseOrderDto> create(@Valid @RequestBody CreateCourseOrderRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @PostMapping("/h5/course-orders/{id}/pay")
    public ApiResponse<CourseOrderDto> pay(@PathVariable Long id) {
        return ApiResponse.ok(service.pay(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/course-orders/{id}/cancel")
    public ApiResponse<CourseOrderDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/course-orders/{id}/refund-request")
    public ApiResponse<?> refund(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) RefundCourseOrderRequest body
    ) {
        return ApiResponse.ok(service.requestRefund(CurrentUser.getId(), id, body));
    }

    @GetMapping("/h5/course-orders/mine")
    public ApiResponse<List<CourseOrderDto>> mine() {
        return ApiResponse.ok(service.mine(CurrentUser.getId()));
    }
}
