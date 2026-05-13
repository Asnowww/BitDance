package com.bitdance.workshop;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.workshop.controller.MerchantWorkshopController;
import com.bitdance.workshop.dto.CreateSessionRequest;
import com.bitdance.workshop.dto.CreateWorkshopRequest;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.service.MerchantWorkshopService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MerchantWorkshopController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MerchantWorkshopControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean MerchantWorkshopService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("77");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("STUDIO_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private WorkshopDetail fix(String publish, String audit) {
        return new WorkshopDetail(
            10L, 1L, 7L, 1L, 1L, "Yumi Hiphop Workshop",
            null, "限定档期", "学院路", "海淀区舞星",
            new BigDecimal("199.00"), 5, 30, null,
            publish, audit, List.of(), false
        );
    }

    @Test
    void create_signedCoach_autoApproved() throws Exception {
        when(service.create(eq(77L), any())).thenReturn(fix("draft", "approved"));
        OffsetDateTime deadline = OffsetDateTime.now().plusDays(3);
        mvc.perform(post("/merchant/workshops")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateWorkshopRequest(
                    1L, 7L, 1L, 1L,
                    "Yumi Hiphop Workshop", null, "限定档期",
                    "学院路", "海淀区舞星", null, null,
                    new BigDecimal("199.00"), 5, 30, deadline, "studio"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.auditStatus").value("approved"))
            .andExpect(jsonPath("$.data.publishStatus").value("draft"));
    }

    @Test
    void create_independentCoach_pendingReview() throws Exception {
        when(service.create(eq(77L), any())).thenReturn(fix("draft", "pending"));
        mvc.perform(post("/merchant/workshops")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateWorkshopRequest(
                    1L, 8L, 1L, 1L,
                    "Solo Workshop", null, "x", "addr", "loc",
                    null, null, new BigDecimal("99.00"),
                    1, 10, null, null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.auditStatus").value("pending"));
    }

    @Test
    void create_notOwner_returnsForbidden() throws Exception {
        when(service.create(eq(77L), any()))
            .thenThrow(new BizException("FORBIDDEN", "你不是该舞室的认领管理员"));
        mvc.perform(post("/merchant/workshops")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateWorkshopRequest(
                    99L, null, 1L, null, "x", null, null,
                    "addr", "loc", null, null,
                    new BigDecimal("0.00"), 1, 1, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void create_blankFields_returns400() throws Exception {
        mvc.perform(post("/merchant/workshops")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void publish_notApproved_returnsBiz() throws Exception {
        when(service.publish(77L, 10L))
            .thenThrow(new BizException("WORKSHOP_NOT_APPROVED", "Workshop 未通过审核"));
        mvc.perform(post("/merchant/workshops/10/publish").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORKSHOP_NOT_APPROVED"));
    }

    @Test
    void publish_ok() throws Exception {
        when(service.publish(77L, 10L)).thenReturn(fix("published", "approved"));
        mvc.perform(post("/merchant/workshops/10/publish").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.publishStatus").value("published"));
    }

    @Test
    void offline_ok() throws Exception {
        when(service.offline(77L, 10L)).thenReturn(fix("offline", "approved"));
        mvc.perform(post("/merchant/workshops/10/offline").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.publishStatus").value("offline"));
    }

    @Test
    void addSession_ok() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(7);
        when(service.addSession(eq(77L), any())).thenReturn(new SessionDto(
            100L, 10L, "Day1", start, start.plusHours(2), 30, 0, 0, "scheduled"
        ));
        mvc.perform(post("/merchant/workshop-sessions")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateSessionRequest(
                    10L, "Day1", start, start.plusHours(2), 30
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.capacity").value(30));
    }

    @Test
    void addSession_endBeforeStart_returnsBiz() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(7);
        when(service.addSession(eq(77L), any()))
            .thenThrow(new BizException("INVALID_ARGUMENT", "结束时间必须晚于开始时间"));
        mvc.perform(post("/merchant/workshop-sessions")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateSessionRequest(
                    10L, "Day1", start, start.minusHours(1), 10
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
