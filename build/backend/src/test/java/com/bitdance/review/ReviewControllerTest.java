package com.bitdance.review;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.review.controller.ReviewController;
import com.bitdance.review.dto.CreateReviewRequest;
import com.bitdance.review.dto.DimensionScoreDto;
import com.bitdance.review.dto.ReviewDto;
import com.bitdance.review.dto.ReviewListResponse;
import com.bitdance.review.dto.ReviewSummary;
import com.bitdance.review.service.ReviewService;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ReviewControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean ReviewService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private ReviewDto fixture(String status, BigDecimal weight, boolean verified) {
        return new ReviewDto(
            500L, 42L, "studio", 1L,
            new BigDecimal("4.50"), "环境不错",
            verified, verified ? "trial" : null,
            weight, status, (short) 0, 0, false,
            OffsetDateTime.now(),
            List.of(new DimensionScoreDto("traffic", "交通", (short) 5),
                new DimensionScoreDto("ambience", "氛围", (short) 4))
        );
    }

    private CreateReviewRequest validBody() {
        return new CreateReviewRequest(
            "studio", 1L, new BigDecimal("4.50"), "环境不错",
            List.of(new DimensionScoreDto("traffic", "交通", (short) 5),
                new DimensionScoreDto("ambience", "氛围", (short) 4)),
            "trial", 999L
        );
    }

    @Test
    void create_published() throws Exception {
        when(service.create(eq(42L), any(CreateReviewRequest.class)))
            .thenReturn(fixture("published", new BigDecimal("1.500"), true));
        mvc.perform(post("/h5/reviews")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(validBody())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewStatus").value("published"))
            .andExpect(jsonPath("$.data.isVerified").value(true))
            .andExpect(jsonPath("$.data.weightFactor").value(1.5))
            .andExpect(jsonPath("$.data.dimensions.length()").value(2));
    }

    @Test
    void create_pendingWhenRisky() throws Exception {
        when(service.create(eq(42L), any())).thenReturn(fixture("pending", new BigDecimal("0.600"), false));
        mvc.perform(post("/h5/reviews")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(validBody())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewStatus").value("pending"));
    }

    @Test
    void create_invalidScore_returns400() throws Exception {
        var bad = new CreateReviewRequest(
            "studio", 1L, new BigDecimal("6.00"), "x",
            List.of(new DimensionScoreDto("a", "交通", (short) 5)),
            null, null
        );
        mvc.perform(post("/h5/reviews")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_invalidTargetType_returns400() throws Exception {
        var bad = new CreateReviewRequest(
            "workshop", 1L, new BigDecimal("4.0"), null,
            List.of(new DimensionScoreDto("a", "交通", (short) 5)),
            null, null
        );
        mvc.perform(post("/h5/reviews")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_emptyDimensions_returns400() throws Exception {
        var bad = new CreateReviewRequest(
            "studio", 1L, new BigDecimal("4.0"), null, List.of(), null, null
        );
        mvc.perform(post("/h5/reviews")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void list_returnsPaged() throws Exception {
        when(service.list(eq("studio"), eq(1L), eq("verified"), eq(null), eq(1), eq(20)))
            .thenReturn(new ReviewListResponse(
                List.of(fixture("published", new BigDecimal("1.500"), true)),
                1, 20, 1L
            ));
        mvc.perform(get("/public/reviews")
                .param("targetType", "studio")
                .param("targetId", "1")
                .param("sort", "verified"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].isVerified").value(true));
    }

    @Test
    void list_invalidPage_returns400() throws Exception {
        mvc.perform(get("/public/reviews")
                .param("targetType", "studio")
                .param("targetId", "1")
                .param("page", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void list_invalidSort_returns400() throws Exception {
        mvc.perform(get("/public/reviews")
                .param("targetType", "studio")
                .param("targetId", "1")
                .param("sort", "random"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void summary_returnsWeightedAvg() throws Exception {
        when(service.summary("studio", 1L)).thenReturn(new ReviewSummary(
            "studio", 1L, 12L, 7L, new BigDecimal("4.32"),
            Map.of("traffic", new BigDecimal("4.5"), "ambience", new BigDecimal("4.1"))
        ));
        mvc.perform(get("/public/reviews/summary")
                .param("targetType", "studio")
                .param("targetId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(12))
            .andExpect(jsonPath("$.data.verifiedCount").value(7))
            .andExpect(jsonPath("$.data.weightedAvgScore").value(4.32))
            .andExpect(jsonPath("$.data.dimensionAvg.traffic").value(4.5));
    }

    @Test
    void delete_ok() throws Exception {
        doNothing().when(service).delete(42L, 500L);
        mvc.perform(delete("/h5/reviews/500").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void delete_otherUser_returnsForbidden() throws Exception {
        doThrow(new BizException("FORBIDDEN", "无权删除他人评价"))
            .when(service).delete(42L, 500L);
        mvc.perform(delete("/h5/reviews/500").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
