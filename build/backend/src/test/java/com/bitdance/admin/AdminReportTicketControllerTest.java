package com.bitdance.admin;

import com.bitdance.admin.controller.AdminReportTicketController;
import com.bitdance.admin.dto.HandleReportRequest;
import com.bitdance.admin.dto.ReportTicketDto;
import com.bitdance.admin.service.AdminReportTicketService;
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

@WebMvcTest(controllers = AdminReportTicketController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminReportTicketControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean AdminReportTicketService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("7");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("PLATFORM_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private ReportTicketDto fix(String status) {
        return new ReportTicketDto(
            900L, 42L, "content_post", 100L,
            "spam", "广告引流", status,
            null, null, null, OffsetDateTime.now()
        );
    }

    @Test
    void list_ok() throws Exception {
        when(service.list(eq("pending"), eq(null), eq(1), eq(20)))
            .thenReturn(new PageImpl<>(List.of(fix("pending")),
                PageRequest.of(0, 20), 1L));
        mvc.perform(get("/admin/report-tickets").param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void process_ok() throws Exception {
        when(service.process(7L, 900L)).thenReturn(fix("processing"));
        mvc.perform(post("/admin/report-tickets/900/process").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reportStatus").value("processing"));
    }

    @Test
    void close_ok() throws Exception {
        when(service.close(eq(7L), eq(900L), any())).thenReturn(fix("closed"));
        mvc.perform(post("/admin/report-tickets/900/close")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new HandleReportRequest("已隐藏内容"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reportStatus").value("closed"));
    }

    @Test
    void reject_ok() throws Exception {
        when(service.reject(eq(7L), eq(900L), any())).thenReturn(fix("rejected"));
        mvc.perform(post("/admin/report-tickets/900/reject")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reportStatus").value("rejected"));
    }

    @Test
    void process_stateConflict_returnsBiz() throws Exception {
        when(service.process(7L, 900L))
            .thenThrow(new BizException("REPORT_STATE_CONFLICT", "工单状态 closed 不可执行该操作"));
        mvc.perform(post("/admin/report-tickets/900/process").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REPORT_STATE_CONFLICT"));
    }
}
