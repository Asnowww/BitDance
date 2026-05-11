package com.bitdance.catalog.controller;

import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.service.CourseService;
import com.bitdance.common.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/public/studios/{studioId}/schedules")
public class StudioScheduleController {

    private final CourseService courseService;

    public StudioScheduleController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<ScheduleItem>> schedules(
        @PathVariable Long studioId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(courseService.schedulesOfStudio(studioId, from, to));
    }
}
