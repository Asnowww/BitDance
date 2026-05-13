package com.bitdance.review;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.review.controller.ReviewAppealController;
import com.bitdance.review.dto.CreateAppealRequest;
import com.bitdance.review.dto.HandleAppealRequest;
import com.bitdance.review.dto.ReviewAppealDto;
import com.bitdance.review.service.ReviewAppealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewAppealController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ReviewAppealControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean ReviewAppealService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("PLATFORM_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private ReviewAppealDto fix(String status) {
        return new ReviewAppealDto(
            300L, 88L, 42L, "评论与事实不符", status, "我有订单凭证",
            "approved".equals(status) || "rejected".equals(status) ? 7L : null,
            "approved".equals(status) || "rejected".equals(status) ? OffsetDateTime.now() : null,
            null, OffsetDateTime.now()
        );
    }

    @Test
    void create_ok() throws Exception {
        when(service.create(eq(42L), any())).thenReturn(fix("pending"));
        mvc.perform(post("/h5/review-appeals")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateAppealRequest(
                    88L, "评论与事实不符", "我有订单凭证"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.appealStatus").value("pending"));
    }

    @Test
    void create_tooShortReason_returns400() throws Exception {
        mvc.perform(post("/h5/review-appeals")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateAppealRequest(88L, "短", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_duplicated_returnsBiz() throws Exception {
        when(service.create(eq(42L), any()))
            .thenThrow(new BizException("APPEAL_DUPLICATED", "已有待处理的申诉"));
        mvc.perform(post("/h5/review-appeals")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateAppealRequest(
                    88L, "评论与事实不符", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("APPEAL_DUPLICATED"));
    }

    @Test
    void admin_list_returnsPage() throws Exception {
        Page<ReviewAppealDto> page = new PageImpl<>(List.of(fix("pending")),
            PageRequest.of(0, 20), 1L);
        when(service.listByStatus(eq("pending"), eq(1), eq(20))).thenReturn(page);
        mvc.perform(get("/admin/review-appeals").param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void admin_approve_ok() throws Exception {
        when(service.approve(eq(42L), eq(300L), any())).thenReturn(fix("approved"));
        mvc.perform(post("/admin/review-appeals/300/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new HandleAppealRequest("证据充分"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.appealStatus").value("approved"));
    }

    @Test
    void admin_reject_ok() throws Exception {
        when(service.reject(eq(42L), eq(300L), any())).thenReturn(fix("rejected"));
        mvc.perform(post("/admin/review-appeals/300/reject")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.appealStatus").value("rejected"));
    }

    @Test
    void admin_handle_stateConflict_returnsBiz() throws Exception {
        when(service.approve(eq(42L), eq(300L), any()))
            .thenThrow(new BizException("APPEAL_STATE_CONFLICT", "申诉状态 approved 不可处理"));
        mvc.perform(post("/admin/review-appeals/300/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("APPEAL_STATE_CONFLICT"));
    }
}
