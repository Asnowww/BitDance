package com.bitdance.coachops;

import com.bitdance.coachops.controller.CoachOpsController;
import com.bitdance.coachops.dto.CoachDashboardDto;
import com.bitdance.coachops.dto.CoachMeDto;
import com.bitdance.coachops.dto.UpdateCoachProfileRequest;
import com.bitdance.coachops.service.CoachOpsService;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CoachOpsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CoachOpsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean CoachOpsService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("COACH"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private CoachMeDto certified() {
        return new CoachMeDto(
            true, 7L, "Yumi", "5 年舞龄", "注重律动",
            "approved", 1L, null, new BigDecimal("4.85"),
            List.of(1L)
        );
    }

    @Test
    void me_certified() throws Exception {
        when(service.me(42L)).thenReturn(certified());
        mvc.perform(get("/h5/coach/me").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.certified").value(true))
            .andExpect(jsonPath("$.data.coachId").value(7));
    }

    @Test
    void me_notCertified_returnsPlaceholder() throws Exception {
        when(service.me(42L)).thenReturn(new CoachMeDto(
            false, null, null, null, null, "not_applied",
            null, null, BigDecimal.ZERO, List.of()
        ));
        mvc.perform(get("/h5/coach/me").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.certified").value(false))
            .andExpect(jsonPath("$.data.certificationStatus").value("not_applied"));
    }

    @Test
    void updateProfile_ok() throws Exception {
        when(service.updateProfile(eq(42L), any())).thenReturn(certified());
        mvc.perform(put("/h5/coach/me/profile")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new UpdateCoachProfileRequest(
                    "Yumi", "5 年舞龄", "注重律动", null, 1L
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName").value("Yumi"));
    }

    @Test
    void updateProfile_notCoach_returnsBiz() throws Exception {
        when(service.updateProfile(eq(42L), any()))
            .thenThrow(new BizException("COACH_NOT_FOUND", "尚未通过教练认证"));
        mvc.perform(put("/h5/coach/me/profile")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new UpdateCoachProfileRequest(
                    null, "x", null, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COACH_NOT_FOUND"));
    }

    @Test
    void updateProfile_overSize_returns400() throws Exception {
        String tooLong = "a".repeat(2001);
        mvc.perform(put("/h5/coach/me/profile")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new UpdateCoachProfileRequest(
                    null, tooLong, null, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void dashboard_ok() throws Exception {
        when(service.dashboard(42L)).thenReturn(new CoachDashboardDto(
            12L, 5L, new BigDecimal("995.00"), 3L,
            new BigDecimal("4.85"), 24L
        ));
        mvc.perform(get("/h5/coach/dashboard").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.monthSessions").value(12))
            .andExpect(jsonPath("$.data.pendingReviewReplies").value(3));
    }

    @Test
    void dashboard_notCoach_returnsBiz() throws Exception {
        when(service.dashboard(42L))
            .thenThrow(new BizException("COACH_NOT_APPROVED", "教练资质尚未通过审核"));
        mvc.perform(get("/h5/coach/dashboard").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COACH_NOT_APPROVED"));
    }

    @Test
    void courses_list() throws Exception {
        when(service.myCourses(42L)).thenReturn(List.of(
            new CoachOpsService.CourseSummaryDto(
                101L, 1L, 1L, "Hiphop 入门", "L1",
                new BigDecimal("99.00"), 60, "published"
            )
        ));
        mvc.perform(get("/h5/coach/courses").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].courseName").value("Hiphop 入门"));
    }

    @Test
    void me_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/coach/me"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
