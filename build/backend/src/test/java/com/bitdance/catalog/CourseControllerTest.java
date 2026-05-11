package com.bitdance.catalog;

import com.bitdance.catalog.controller.CourseController;
import com.bitdance.catalog.dto.CourseDetail;
import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.service.CourseService;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CourseControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CourseService courseService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("11");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void detail_returns() throws Exception {
        when(courseService.detail(eq(101L), any())).thenReturn(new CourseDetail(
            101L, 1L, 7L, 1L, "Hiphop 入门", "L1", "{零基础,小白}",
            new BigDecimal("99.00"), 60, "medium", "regular",
            true, "适合零基础", null, "published", false
        ));
        mvc.perform(get("/public/courses/101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courseName").value("Hiphop 入门"))
            .andExpect(jsonPath("$.data.zeroBasicFriendly").value(true));
    }

    @Test
    void detail_offline_returns400Biz() throws Exception {
        when(courseService.detail(eq(202L), any()))
            .thenThrow(new BizException("COURSE_OFFLINE", "课程已下架"));
        mvc.perform(get("/public/courses/202"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COURSE_OFFLINE"));
    }

    @Test
    void schedules_returnsList() throws Exception {
        OffsetDateTime start = LocalDate.of(2026, 5, 12).atTime(19, 0).atOffset(ZoneOffset.UTC);
        when(courseService.schedulesOfCourse(eq(101L), any(), any())).thenReturn(List.of(
            new ScheduleItem(1L, 101L, 1L, 7L, "A1", start, start.plusHours(1), 30, 5, "scheduled")
        ));
        mvc.perform(get("/public/courses/101/schedules")
                .param("from", "2026-05-11")
                .param("to", "2026-05-17"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].capacity").value(30));
    }

    @Test
    void schedules_invalidRange_returns400() throws Exception {
        when(courseService.schedulesOfCourse(eq(101L), any(), any()))
            .thenThrow(new BizException("INVALID_ARGUMENT", "结束日期不能早于开始日期"));
        mvc.perform(get("/public/courses/101/schedules")
                .param("from", "2026-05-20")
                .param("to", "2026-05-11"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
