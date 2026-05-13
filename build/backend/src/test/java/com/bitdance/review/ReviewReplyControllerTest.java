package com.bitdance.review;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.review.controller.ReviewReplyController;
import com.bitdance.review.dto.CreateReplyRequest;
import com.bitdance.review.dto.ReviewReplyDto;
import com.bitdance.review.service.ReviewReplyService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewReplyController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ReviewReplyControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean ReviewReplyService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("COACH"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void create_ok() throws Exception {
        when(service.create(eq(42L), any())).thenReturn(new ReviewReplyDto(
            1L, 88L, 42L, "感谢反馈", false, OffsetDateTime.now()
        ));
        mvc.perform(post("/h5/review-replies")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateReplyRequest(88L, "感谢反馈", false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.replyContent").value("感谢反馈"));
    }

    @Test
    void create_blank_returns400() throws Exception {
        mvc.perform(post("/h5/review-replies")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateReplyRequest(88L, "", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_reviewNotFound_returnsBiz() throws Exception {
        when(service.create(eq(42L), any()))
            .thenThrow(new BizException("REVIEW_NOT_FOUND", "评价不存在"));
        mvc.perform(post("/h5/review-replies")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateReplyRequest(999L, "x", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"));
    }

    @Test
    void delete_other_returnsForbidden() throws Exception {
        doThrow(new BizException("FORBIDDEN", "无权删除他人回复"))
            .when(service).delete(42L, 1L);
        mvc.perform(delete("/h5/review-replies/1").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void listByReview_returns() throws Exception {
        when(service.listByReview(88L)).thenReturn(List.of(
            new ReviewReplyDto(1L, 88L, 42L, "感谢", false, OffsetDateTime.now())
        ));
        mvc.perform(get("/public/review-replies").param("reviewId", "88"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }
}
