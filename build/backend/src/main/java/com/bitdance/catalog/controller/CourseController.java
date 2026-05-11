package com.bitdance.catalog.controller;

import com.bitdance.catalog.dto.CourseDetail;
import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.service.CourseService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/public/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(courseService.detail(id, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/{id}/schedules")
    public ApiResponse<List<ScheduleItem>> schedules(
        @PathVariable Long id,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(courseService.schedulesOfCourse(id, from, to));
    }
}
