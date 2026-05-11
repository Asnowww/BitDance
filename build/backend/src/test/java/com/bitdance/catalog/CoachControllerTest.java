package com.bitdance.catalog;

import com.bitdance.catalog.controller.CoachController;
import com.bitdance.catalog.dto.CoachDetail;
import com.bitdance.catalog.dto.CoachStyleDto;
import com.bitdance.catalog.dto.CourseCard;
import com.bitdance.catalog.service.CoachService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CoachController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CoachControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CoachService coachService;
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
        when(coachService.detail(eq(7L), any())).thenReturn(new CoachDetail(
            7L, 1001L, "Yumi", "7 年舞龄", "注重律动", "[]",
            "approved", 1L, null, new BigDecimal("4.80"),
            List.of(new CoachStyleDto(1L, "advanced")), false
        ));
        mvc.perform(get("/public/coaches/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName").value("Yumi"))
            .andExpect(jsonPath("$.data.styles[0].danceStyleId").value(1));
    }

    @Test
    void detail_notFound_returns400Biz() throws Exception {
        when(coachService.detail(eq(999L), any()))
            .thenThrow(new BizException("COACH_NOT_FOUND", "教练不存在"));
        mvc.perform(get("/public/coaches/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COACH_NOT_FOUND"));
    }

    @Test
    void courses_returnsList() throws Exception {
        when(coachService.coursesOfCoach(7L)).thenReturn(List.of(
            new CourseCard(101L, 1L, 7L, 1L, "Hiphop 入门", "L1",
                new BigDecimal("99.00"), 60, true, null),
            new CourseCard(102L, 1L, 7L, 1L, "Hiphop 进阶", "L2",
                new BigDecimal("139.00"), 75, false, null)
        ));
        mvc.perform(get("/public/coaches/7/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].courseName").value("Hiphop 入门"));
    }
}
