package com.bitdance.coachops;

import com.bitdance.coachops.controller.CoachCertificationController;
import com.bitdance.coachops.dto.CertificationDto;
import com.bitdance.coachops.dto.HandleCertificationRequest;
import com.bitdance.coachops.dto.SubmitCertificationRequest;
import com.bitdance.coachops.service.CoachCertificationService;
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

@WebMvcTest(controllers = CoachCertificationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CoachCertificationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean CoachCertificationService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private CertificationDto fix(String status) {
        return new CertificationDto(
            500L, 42L, "independent", status, "5 年舞龄",
            "approved".equals(status) || "rejected".equals(status) ? 7L : null,
            "approved".equals(status) || "rejected".equals(status) ? OffsetDateTime.now() : null,
            null, OffsetDateTime.now()
        );
    }

    @Test
    void submit_ok() throws Exception {
        when(service.submit(eq(42L), any())).thenReturn(fix("pending"));
        mvc.perform(post("/h5/coach/certifications")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitCertificationRequest("independent", "5 年舞龄"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationStatus").value("pending"));
    }

    @Test
    void submit_duplicated_returnsBiz() throws Exception {
        when(service.submit(eq(42L), any()))
            .thenThrow(new BizException("CERT_DUPLICATED", "已有待处理的资质申请"));
        mvc.perform(post("/h5/coach/certifications")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitCertificationRequest(null, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CERT_DUPLICATED"));
    }

    @Test
    void submit_invalidType_returns400() throws Exception {
        mvc.perform(post("/h5/coach/certifications")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitCertificationRequest("freelance", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void mine_list() throws Exception {
        when(service.mine(42L)).thenReturn(List.of(fix("pending"), fix("approved")));
        mvc.perform(get("/h5/coach/certifications/mine").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void admin_list_returnsPage() throws Exception {
        when(service.listByStatus(eq("pending"), eq(1), eq(20)))
            .thenReturn(new PageImpl<>(List.of(fix("pending")), PageRequest.of(0, 20), 1L));
        mvc.perform(get("/admin/coach-certifications").param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void admin_approve_ok() throws Exception {
        when(service.approve(eq(42L), eq(500L), any())).thenReturn(fix("approved"));
        mvc.perform(post("/admin/coach-certifications/500/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new HandleCertificationRequest("证件齐全"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationStatus").value("approved"));
    }

    @Test
    void admin_reject_ok() throws Exception {
        when(service.reject(eq(42L), eq(500L), any())).thenReturn(fix("rejected"));
        mvc.perform(post("/admin/coach-certifications/500/reject")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationStatus").value("rejected"));
    }

    @Test
    void admin_handle_stateConflict_returnsBiz() throws Exception {
        when(service.approve(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("CERT_STATE_CONFLICT", "申请状态 approved 不可处理"));
        mvc.perform(post("/admin/coach-certifications/500/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CERT_STATE_CONFLICT"));
    }
}
