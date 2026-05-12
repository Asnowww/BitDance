package com.bitdance.growth;

import com.bitdance.common.exception.BizException;
import com.bitdance.growth.controller.GrowthController;
import com.bitdance.growth.dto.BadgeDto;
import com.bitdance.growth.dto.CheckinDto;
import com.bitdance.growth.dto.CreateCheckinRequest;
import com.bitdance.growth.dto.CreateWorkRequest;
import com.bitdance.growth.dto.GoalDto;
import com.bitdance.growth.dto.GrowthStats;
import com.bitdance.growth.dto.TimelineItem;
import com.bitdance.growth.dto.UpsertGoalRequest;
import com.bitdance.growth.dto.WorkDto;
import com.bitdance.growth.service.GrowthService;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GrowthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class GrowthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean GrowthService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void createCheckin_ok() throws Exception {
        when(service.createCheckin(eq(42L), any())).thenReturn(new CheckinDto(
            1L, 42L, 3L, null, null, null,
            60, "练得不错", true, OffsetDateTime.now()
        ));
        var body = new CreateCheckinRequest(3L, null, null, null,
            60, "练得不错", true, null);
        mvc.perform(post("/h5/growth/checkins")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.durationMinutes").value(60));
    }

    @Test
    void createCheckin_missingDuration_returns400() throws Exception {
        mvc.perform(post("/h5/growth/checkins")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void createCheckin_overMax_returns400() throws Exception {
        var body = new CreateCheckinRequest(null, null, null, null, 5000, null, null, null);
        mvc.perform(post("/h5/growth/checkins")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void listCheckins_returnsItems() throws Exception {
        when(service.listCheckins(42L)).thenReturn(List.of(
            new CheckinDto(1L, 42L, null, null, null, null, 60, "x", true, OffsetDateTime.now()),
            new CheckinDto(2L, 42L, null, null, null, null, 45, "y", true, OffsetDateTime.now())
        ));
        mvc.perform(get("/h5/growth/checkins").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void deleteCheckin_otherUser_returnsForbidden() throws Exception {
        doThrow(new BizException("FORBIDDEN", "无权删除他人打卡"))
            .when(service).deleteCheckin(42L, 99L);
        mvc.perform(delete("/h5/growth/checkins/99").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void deleteCheckin_ok() throws Exception {
        doNothing().when(service).deleteCheckin(42L, 1L);
        mvc.perform(delete("/h5/growth/checkins/1").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void stats_returns() throws Exception {
        when(service.stats(42L)).thenReturn(new GrowthStats(
            12L, 720L, 8, 3, 4, OffsetDateTime.now()
        ));
        mvc.perform(get("/h5/growth/stats").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalSessions").value(12))
            .andExpect(jsonPath("$.data.streakDays").value(4));
    }

    @Test
    void timeline_returns() throws Exception {
        when(service.timeline(42L)).thenReturn(List.of(
            new TimelineItem("checkin", 1L, "训练打卡", "60 分钟", OffsetDateTime.now()),
            new TimelineItem("work", 1L, "阶段作品 · Routine v1", null, OffsetDateTime.now().minusDays(1))
        ));
        mvc.perform(get("/h5/growth/timeline").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].type").value("checkin"));
    }

    @Test
    void upsertGoal_ok() throws Exception {
        when(service.upsertActiveGoal(eq(42L), any())).thenReturn(new GoalDto(
            1L, 42L, "weekly", 300, 5, 60, 1,
            LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 17), "active"
        ));
        var body = new UpsertGoalRequest("weekly", 300, 5,
            LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 17));
        mvc.perform(put("/h5/growth/goals/active")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.goalPeriod").value("weekly"))
            .andExpect(jsonPath("$.data.targetTimes").value(5));
    }

    @Test
    void upsertGoal_invalidPeriod_returns400() throws Exception {
        var body = new UpsertGoalRequest("yearly", 100, 5,
            LocalDate.now(), LocalDate.now().plusDays(7));
        mvc.perform(put("/h5/growth/goals/active")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void activeGoal_returns() throws Exception {
        when(service.activeGoal(42L)).thenReturn(new GoalDto(
            1L, 42L, "monthly", 1200, 16, 240, 4,
            LocalDate.now().withDayOfMonth(1), LocalDate.now().plusDays(20), "active"
        ));
        mvc.perform(get("/h5/growth/goals/active").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.goalPeriod").value("monthly"));
    }

    @Test
    void createWork_ok() throws Exception {
        when(service.createWork(eq(42L), any())).thenReturn(new WorkDto(
            1L, 42L, 1L, "Routine v1", "第一支完整 Routine", null, true, OffsetDateTime.now()
        ));
        var body = new CreateWorkRequest(1L, "Routine v1", "第一支完整 Routine", null, true);
        mvc.perform(post("/h5/growth/works")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.workTitle").value("Routine v1"));
    }

    @Test
    void createWork_missingTitle_returns400() throws Exception {
        var body = new CreateWorkRequest(null, "", null, null, null);
        mvc.perform(post("/h5/growth/works")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void listWorks_returnsItems() throws Exception {
        when(service.listWorks(42L)).thenReturn(List.of(
            new WorkDto(1L, 42L, null, "T1", null, null, true, OffsetDateTime.now())
        ));
        mvc.perform(get("/h5/growth/works").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void deleteWork_ok() throws Exception {
        doNothing().when(service).deleteWork(42L, 1L);
        mvc.perform(delete("/h5/growth/works/1").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void badges_list() throws Exception {
        when(service.listBadges(42L)).thenReturn(List.of(
            new BadgeDto(1L, 100L, "checkin_streak", 7L, OffsetDateTime.now())
        ));
        mvc.perform(get("/h5/growth/badges").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].badgeId").value(100));
    }

    @Test
    void list_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/growth/stats"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
