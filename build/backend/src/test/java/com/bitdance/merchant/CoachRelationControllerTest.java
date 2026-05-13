package com.bitdance.merchant;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.merchant.controller.CoachRelationController;
import com.bitdance.merchant.dto.InviteCoachRequest;
import com.bitdance.merchant.dto.StudioCoachRelationDto;
import com.bitdance.merchant.dto.UpdateCoachRelationRequest;
import com.bitdance.merchant.service.CoachRelationService;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CoachRelationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CoachRelationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean CoachRelationService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("77");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("STUDIO_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private StudioCoachRelationDto fix(String status, String type) {
        return new StudioCoachRelationDto(
            900L, 1L, 7L, type, status,
            "ratio", new BigDecimal("70.00"),
            77L, "active".equals(status) ? 77L : null,
            LocalDate.now(), null, OffsetDateTime.now()
        );
    }

    @Test
    void invite_ok() throws Exception {
        when(service.invite(eq(77L), any())).thenReturn(fix("pending", "signed"));
        mvc.perform(post("/merchant/coach-relations")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new InviteCoachRequest(
                    1L, 7L, "signed", "ratio", new BigDecimal("70.00"), null, null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationStatus").value("pending"));
    }

    @Test
    void invite_notOwner_returnsForbidden() throws Exception {
        when(service.invite(eq(77L), any()))
            .thenThrow(new BizException("FORBIDDEN", "你不是该舞室的认领管理员"));
        mvc.perform(post("/merchant/coach-relations")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new InviteCoachRequest(
                    1L, 7L, "signed", null, null, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void invite_duplicated_returnsBiz() throws Exception {
        when(service.invite(eq(77L), any()))
            .thenThrow(new BizException("RELATION_DUPLICATED", "已有进行中的合作关系"));
        mvc.perform(post("/merchant/coach-relations")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new InviteCoachRequest(
                    1L, 7L, "signed", null, null, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("RELATION_DUPLICATED"));
    }

    @Test
    void invite_invalidType_returns400() throws Exception {
        mvc.perform(post("/merchant/coach-relations")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new InviteCoachRequest(
                    1L, 7L, "casual", null, null, null, null
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void update_active_ok() throws Exception {
        when(service.update(eq(77L), eq(900L), any())).thenReturn(fix("active", "signed"));
        mvc.perform(put("/merchant/coach-relations/900")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new UpdateCoachRelationRequest(
                    "active", null, null, null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationStatus").value("active"));
    }

    @Test
    void list_byStudio_ok() throws Exception {
        when(service.listByStudio(77L, 1L)).thenReturn(List.of(
            fix("active", "signed"), fix("pending", "independent")
        ));
        mvc.perform(get("/merchant/coach-relations").param("studioId", "1")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }
}
