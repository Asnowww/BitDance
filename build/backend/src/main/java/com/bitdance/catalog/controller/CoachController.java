package com.bitdance.catalog.controller;

import com.bitdance.catalog.dto.CoachDetail;
import com.bitdance.catalog.dto.CourseCard;
import com.bitdance.catalog.service.CoachService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/coaches")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping("/{id}")
    public ApiResponse<CoachDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(coachService.detail(id, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/{id}/courses")
    public ApiResponse<List<CourseCard>> courses(@PathVariable Long id) {
        return ApiResponse.ok(coachService.coursesOfCoach(id));
    }
}
