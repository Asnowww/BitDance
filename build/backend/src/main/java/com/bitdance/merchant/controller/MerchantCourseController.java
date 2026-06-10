package com.bitdance.merchant.controller;

import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.courseorder.dto.CourseOrderDto;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.merchant.dto.MerchantCourseDto;
import com.bitdance.merchant.dto.MerchantCourseRequest;
import com.bitdance.merchant.dto.MerchantScheduleRequest;
import com.bitdance.merchant.service.MerchantCourseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class MerchantCourseController {

    private final MerchantCourseService service;

    public MerchantCourseController(MerchantCourseService service) {
        this.service = service;
    }

    @PostMapping("/merchant/courses")
    public ApiResponse<MerchantCourseDto> create(@Valid @RequestBody MerchantCourseRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @PutMapping("/merchant/courses/{id}")
    public ApiResponse<MerchantCourseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody MerchantCourseRequest body
    ) {
        return ApiResponse.ok(service.update(CurrentUser.getId(), id, body));
    }

    @PostMapping("/merchant/courses/{id}/publish")
    public ApiResponse<MerchantCourseDto> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publish(CurrentUser.getId(), id));
    }

    @PostMapping("/merchant/courses/{id}/offline")
    public ApiResponse<MerchantCourseDto> offline(@PathVariable Long id) {
        return ApiResponse.ok(service.offline(CurrentUser.getId(), id));
    }

    @GetMapping("/merchant/courses")
    public ApiResponse<List<MerchantCourseDto>> list(
        @RequestParam Long studioId,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.list(CurrentUser.getId(), studioId, status));
    }

    @PostMapping("/merchant/course-schedules")
    public ApiResponse<ScheduleItem> createSchedule(@Valid @RequestBody MerchantScheduleRequest body) {
        return ApiResponse.ok(service.createSchedule(CurrentUser.getId(), body));
    }

    @PutMapping("/merchant/course-schedules/{id}")
    public ApiResponse<ScheduleItem> updateSchedule(
        @PathVariable Long id,
        @Valid @RequestBody MerchantScheduleRequest body
    ) {
        return ApiResponse.ok(service.updateSchedule(CurrentUser.getId(), id, body));
    }

    @PostMapping("/merchant/course-schedules/{id}/cancel")
    public ApiResponse<ScheduleItem> cancelSchedule(@PathVariable Long id) {
        return ApiResponse.ok(service.cancelSchedule(CurrentUser.getId(), id));
    }

    @GetMapping("/merchant/course-schedules/week")
    public ApiResponse<List<ScheduleItem>> week(
        @RequestParam Long studioId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ApiResponse.ok(service.week(CurrentUser.getId(), studioId, from, to));
    }

    @GetMapping("/merchant/course-schedules/{id}/bookings")
    public ApiResponse<List<CourseOrderDto>> bookings(@PathVariable Long id) {
        return ApiResponse.ok(service.bookings(CurrentUser.getId(), id));
    }
}
