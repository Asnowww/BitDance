package com.bitdance.buddy;

import com.bitdance.buddy.controller.BuddyController;
import com.bitdance.buddy.dto.BuddyDto;
import com.bitdance.buddy.dto.CreateRatingRequest;
import com.bitdance.buddy.dto.RatingDto;
import com.bitdance.buddy.service.BuddyService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuddyController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class BuddyControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean BuddyService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private RatingDto ratingFixture() {
        return new RatingDto(
            900L, 100L, 42L, 99L,
            (short) 5, (short) 5, (short) 4,
            "守时友好", OffsetDateTime.now()
        );
    }

    private BuddyDto buddyFixture(String status) {
        return new BuddyDto(11L, 99L, 100L, status, OffsetDateTime.now());
    }

    @Test
    void rate_ok() throws Exception {
        when(service.rate(eq(42L), eq(100L), any())).thenReturn(ratingFixture());
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    99L, (short) 5, (short) 5, (short) 4, "守时友好"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.toUserId").value(99))
            .andExpect(jsonPath("$.data.punctualityScore").value(5))
            .andExpect(jsonPath("$.data.skillMatchScore").value(4));
    }

    @Test
    void rate_self_returns400() throws Exception {
        when(service.rate(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("INVALID_ARGUMENT", "不能评价自己"));
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    42L, (short) 5, (short) 5, (short) 5, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void rate_outOfRange_returns400() throws Exception {
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    99L, (short) 6, (short) 5, (short) 5, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void rate_missingScore_returns400() throws Exception {
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toUserId\":99}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void rate_duplicated_returnsBiz() throws Exception {
        when(service.rate(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("RATING_DUPLICATED", "已对该参与者评价过"));
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    99L, (short) 5, (short) 5, (short) 5, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("RATING_DUPLICATED"));
    }

    @Test
    void rate_nonParticipant_returnsForbidden() throws Exception {
        when(service.rate(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("FORBIDDEN", "你不是该约练的参与者"));
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    99L, (short) 5, (short) 5, (short) 5, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void rate_stateConflict_returnsBiz() throws Exception {
        when(service.rate(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("PRACTICE_STATE_CONFLICT", "当前状态 published 不可评价"));
        mvc.perform(post("/h5/practices/100/ratings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateRatingRequest(
                    99L, (short) 5, (short) 5, (short) 5, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PRACTICE_STATE_CONFLICT"));
    }

    @Test
    void ratings_returnsListForParticipant() throws Exception {
        when(service.ratingsOfPost(42L, 100L)).thenReturn(List.of(ratingFixture()));
        mvc.perform(get("/h5/practices/100/ratings").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void ratings_nonParticipant_returnsForbidden() throws Exception {
        when(service.ratingsOfPost(42L, 100L))
            .thenThrow(new BizException("FORBIDDEN", "你不是该约练的参与者"));
        mvc.perform(get("/h5/practices/100/ratings").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void myBuddies_active() throws Exception {
        when(service.listMyBuddies(42L, "active"))
            .thenReturn(List.of(buddyFixture("active"), buddyFixture("active")));
        mvc.perform(get("/h5/buddies").param("status", "active")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void block_ok() throws Exception {
        when(service.block(42L, 99L)).thenReturn(buddyFixture("blocked"));
        mvc.perform(post("/h5/buddies/99/block").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationStatus").value("blocked"));
    }

    @Test
    void block_notFound_returnsBiz() throws Exception {
        when(service.block(42L, 99L))
            .thenThrow(new BizException("BUDDY_NOT_FOUND", "搭子关系不存在"));
        mvc.perform(post("/h5/buddies/99/block").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BUDDY_NOT_FOUND"));
    }

    @Test
    void remove_ok() throws Exception {
        when(service.removeBuddy(42L, 99L)).thenReturn(buddyFixture("inactive"));
        mvc.perform(delete("/h5/buddies/99").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationStatus").value("inactive"));
    }

    @Test
    void myBuddies_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/buddies"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
