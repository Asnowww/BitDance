package com.bitdance.catalog;

import com.bitdance.catalog.controller.StudioScheduleController;
import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.service.CourseService;
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

@WebMvcTest(controllers = StudioScheduleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class StudioScheduleControllerTest {

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
    void schedulesOfStudio_returnsList() throws Exception {
        OffsetDateTime start = LocalDate.of(2026, 5, 12).atTime(19, 0).atOffset(ZoneOffset.UTC);
        when(courseService.schedulesOfStudio(eq(1L), any(), any())).thenReturn(List.of(
            new ScheduleItem(11L, 101L, 1L, 7L, "A1", start, start.plusHours(1), 30, 5, "scheduled"),
            new ScheduleItem(12L, 102L, 1L, 8L, "B2", start.plusHours(2), start.plusHours(3), 20, 0, "scheduled")
        ));
        mvc.perform(get("/public/studios/1/schedules")
                .param("from", "2026-05-11")
                .param("to", "2026-05-17"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[1].courseId").value(102));
    }

    @Test
    void schedulesOfStudio_defaultRange_ok() throws Exception {
        when(courseService.schedulesOfStudio(eq(1L), any(), any())).thenReturn(List.of());
        mvc.perform(get("/public/studios/1/schedules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
