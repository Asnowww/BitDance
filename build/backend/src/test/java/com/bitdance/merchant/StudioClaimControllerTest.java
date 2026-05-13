package com.bitdance.merchant;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.merchant.controller.StudioClaimController;
import com.bitdance.merchant.dto.HandleClaimRequest;
import com.bitdance.merchant.dto.StudioClaimDto;
import com.bitdance.merchant.dto.SubmitClaimRequest;
import com.bitdance.merchant.service.StudioClaimService;
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

@WebMvcTest(controllers = StudioClaimController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class StudioClaimControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean StudioClaimService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private StudioClaimDto fix(String status) {
        return new StudioClaimDto(
            300L, 1L, 42L, "owner_claim", status, null, "我是店主",
            "approved".equals(status) || "rejected".equals(status) ? 7L : null,
            "approved".equals(status) || "rejected".equals(status) ? OffsetDateTime.now() : null,
            null, OffsetDateTime.now()
        );
    }

    @Test
    void submit_ok() throws Exception {
        when(service.submit(eq(42L), any())).thenReturn(fix("pending"));
        mvc.perform(post("/h5/studio-claims")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitClaimRequest(
                    1L, "owner_claim", null, "我是店主"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.claimStatus").value("pending"));
    }

    @Test
    void submit_duplicated_returnsBiz() throws Exception {
        when(service.submit(eq(42L), any()))
            .thenThrow(new BizException("CLAIM_DUPLICATED", "已有待处理的认领申请"));
        mvc.perform(post("/h5/studio-claims")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitClaimRequest(1L, null, null, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CLAIM_DUPLICATED"));
    }

    @Test
    void submit_studioNotFound_returnsBiz() throws Exception {
        when(service.submit(eq(42L), any()))
            .thenThrow(new BizException("STUDIO_NOT_FOUND", "舞室不存在"));
        mvc.perform(post("/h5/studio-claims")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SubmitClaimRequest(999L, null, null, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("STUDIO_NOT_FOUND"));
    }

    @Test
    void mine_list() throws Exception {
        when(service.mine(42L)).thenReturn(List.of(fix("pending"), fix("approved")));
        mvc.perform(get("/h5/studio-claims/mine").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void admin_list_returnsPage() throws Exception {
        when(service.listByStatus(eq("pending"), eq(1), eq(20)))
            .thenReturn(new PageImpl<>(List.of(fix("pending")), PageRequest.of(0, 20), 1L));
        mvc.perform(get("/admin/studio-claims").param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void admin_approve_ok() throws Exception {
        when(service.approve(eq(42L), eq(300L), any())).thenReturn(fix("approved"));
        mvc.perform(post("/admin/studio-claims/300/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new HandleClaimRequest("证件齐全"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.claimStatus").value("approved"));
    }

    @Test
    void admin_reject_ok() throws Exception {
        when(service.reject(eq(42L), eq(300L), any())).thenReturn(fix("rejected"));
        mvc.perform(post("/admin/studio-claims/300/reject")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.claimStatus").value("rejected"));
    }

    @Test
    void admin_handle_stateConflict_returnsBiz() throws Exception {
        when(service.approve(eq(42L), eq(300L), any()))
            .thenThrow(new BizException("CLAIM_STATE_CONFLICT", "申请状态 approved 不可处理"));
        mvc.perform(post("/admin/studio-claims/300/approve")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CLAIM_STATE_CONFLICT"));
    }
}
