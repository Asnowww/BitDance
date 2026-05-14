package com.bitdance.workshop;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.workshop.controller.AdminWorkshopController;
import com.bitdance.workshop.dto.WorkshopAdminItem;
import com.bitdance.workshop.service.AdminWorkshopService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminWorkshopController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminWorkshopControllerTest {

    @Autowired MockMvc mvc;
    @MockBean AdminWorkshopService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("7");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("PLATFORM_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private WorkshopAdminItem fix(String audit, String publish) {
        return new WorkshopAdminItem(
            10L, 1L, 7L, 1L, "Yumi Hiphop",
            new BigDecimal("199.00"), null, audit, publish
        );
    }

    @Test
    void list_pending_ok() throws Exception {
        when(service.listByAuditStatus(eq("pending"), eq(1), eq(20)))
            .thenReturn(new PageImpl<>(List.of(fix("pending", "draft")),
                PageRequest.of(0, 20), 1L));
        mvc.perform(get("/admin/workshops").param("auditStatus", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void approve_ok() throws Exception {
        when(service.approve(7L, 10L)).thenReturn(fix("approved", "draft"));
        mvc.perform(post("/admin/workshops/10/approve").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.auditStatus").value("approved"));
    }

    @Test
    void reject_ok() throws Exception {
        when(service.reject(7L, 10L)).thenReturn(fix("rejected", "draft"));
        mvc.perform(post("/admin/workshops/10/reject").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.auditStatus").value("rejected"));
    }

    @Test
    void approve_stateConflict_returnsBiz() throws Exception {
        when(service.approve(7L, 10L))
            .thenThrow(new BizException("WORKSHOP_AUDIT_STATE_CONFLICT",
                "审核状态 approved 不可处理"));
        mvc.perform(post("/admin/workshops/10/approve").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORKSHOP_AUDIT_STATE_CONFLICT"));
    }
}
