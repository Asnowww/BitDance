package com.bitdance.practice;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.practice.controller.PracticeController;
import com.bitdance.practice.dto.CreatePracticeRequest;
import com.bitdance.practice.dto.JoinPracticeRequest;
import com.bitdance.practice.dto.JoinRequestDto;
import com.bitdance.practice.dto.PracticeListResponse;
import com.bitdance.practice.dto.PracticePostDto;
import com.bitdance.practice.service.PracticeService;
import com.bitdance.profile.service.ProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PracticeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PracticeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean PracticeService service;
    @MockBean ProfileService profileService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private PracticePostDto postFixture(String status, int current) {
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        return new PracticePostDto(
            100L, 42L, 1L, null, 1L,
            "海淀区舞星 Studio", "学院路 1 号",
            new BigDecimal("116.401"), new BigDecimal("39.901"),
            "intermediate", 2, 4, current,
            start, start.plusHours(2), start, status, "练 Hiphop", OffsetDateTime.now(), null
        );
    }

    private JoinRequestDto joinFixture(String status) {
        return new JoinRequestDto(
            900L, 100L, 99L, status, "求带", 42L,
            "pending".equals(status) ? null : OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    @Test
    void create_published() throws Exception {
        when(service.create(eq(42L), any())).thenReturn(postFixture("published", 1));
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        var body = new CreatePracticeRequest(
            1L, 1L, null, "海淀区舞星 Studio", "学院路 1 号",
            new BigDecimal("116.401"), new BigDecimal("39.901"),
            "intermediate", 2, 4, start, start.plusHours(2), "练 Hiphop"
        );
        mvc.perform(post("/h5/practices")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.postStatus").value("published"))
            .andExpect(jsonPath("$.data.currentPeopleCount").value(1));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        mvc.perform(post("/h5/practices")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_endBeforeStart_serviceRejects() throws Exception {
        when(service.create(eq(42L), any()))
            .thenThrow(new BizException("INVALID_ARGUMENT", "结束时间必须晚于开始时间"));
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        var body = new CreatePracticeRequest(
            1L, 1L, null, "x", null, null, null, null, 2, 4,
            start, start.minusHours(1), null
        );
        mvc.perform(post("/h5/practices")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void square_returns() throws Exception {
        when(service.square(eq(1L), eq(2L), eq("intermediate"), any(), any(), any(), any(), eq(1), eq(20)))
            .thenReturn(new PracticeListResponse(
                List.of(postFixture("published", 1)), 1, 20, 1L
            ));
        mvc.perform(get("/public/practices")
                .param("cityId", "1")
                .param("danceStyleId", "2")
                .param("skillLevel", "intermediate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].postStatus").value("published"));
    }

    @Test
    void detail_returns() throws Exception {
        when(service.detail(100L)).thenReturn(postFixture("matched", 2));
        mvc.perform(get("/public/practices/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.postStatus").value("matched"))
            .andExpect(jsonPath("$.data.currentPeopleCount").value(2));
    }

    @Test
    void cancel_byOther_returnsForbidden() throws Exception {
        when(service.cancel(eq(42L), eq(100L)))
            .thenThrow(new BizException("FORBIDDEN", "无权取消他人约练"));
        mvc.perform(post("/h5/practices/100/cancel").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void apply_returnsPending() throws Exception {
        when(service.apply(eq(42L), eq(100L), any())).thenReturn(joinFixture("pending"));
        mvc.perform(post("/h5/practices/100/join")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new JoinPracticeRequest("求带"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.joinStatus").value("pending"));
    }

    @Test
    void apply_full_returnsBiz() throws Exception {
        when(service.apply(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("PRACTICE_FULL", "已满员"));
        mvc.perform(post("/h5/practices/100/join")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PRACTICE_FULL"));
    }

    @Test
    void apply_ownPost_returns400() throws Exception {
        when(service.apply(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("INVALID_ARGUMENT", "不能申请加入自己的约练"));
        mvc.perform(post("/h5/practices/100/join")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void accept_returnsAccepted() throws Exception {
        when(service.accept(42L, 900L)).thenReturn(joinFixture("accepted"));
        mvc.perform(post("/h5/practice-requests/900/accept").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.joinStatus").value("accepted"));
    }

    @Test
    void reject_returnsRejected() throws Exception {
        when(service.reject(42L, 900L)).thenReturn(joinFixture("rejected"));
        mvc.perform(post("/h5/practice-requests/900/reject").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.joinStatus").value("rejected"));
    }

    @Test
    void cancelByApplicant_returnsCanceled() throws Exception {
        when(service.cancelByApplicant(42L, 900L)).thenReturn(joinFixture("canceled"));
        mvc.perform(post("/h5/practice-requests/900/cancel").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.joinStatus").value("canceled"));
    }

    @Test
    void requestsOfPost_returnsList() throws Exception {
        when(service.requestsOfPost(42L, 100L))
            .thenReturn(List.of(joinFixture("pending"), joinFixture("accepted")));
        mvc.perform(get("/h5/practices/100/requests").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void requestsOfPost_otherCreator_returnsForbidden() throws Exception {
        when(service.requestsOfPost(42L, 100L))
            .thenThrow(new BizException("FORBIDDEN", "无权操作他人约练的申请"));
        mvc.perform(get("/h5/practices/100/requests").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void myPosts_list() throws Exception {
        when(service.myPosts(42L)).thenReturn(List.of(postFixture("published", 1)));
        mvc.perform(get("/h5/practices/mine").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void myJoinRequests_list() throws Exception {
        when(service.myJoinRequests(42L)).thenReturn(List.of(joinFixture("pending")));
        mvc.perform(get("/h5/practice-requests/mine").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void list_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/practices/mine"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
