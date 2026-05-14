package com.bitdance.workshop;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.workshop.controller.MerchantWorkshopCheckinController;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.service.MerchantWorkshopCheckinService;
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

@WebMvcTest(controllers = MerchantWorkshopCheckinController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MerchantWorkshopCheckinControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean MerchantWorkshopCheckinService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("77");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("STUDIO_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private OrderDto fix() {
        return new OrderDto(
            500L, "WS123", 10L, 100L, 42L,
            new BigDecimal("199.00"), new BigDecimal("199.00"),
            "paid", "MOCK-TXN", "ABC12345",
            OffsetDateTime.now(), null, null, OffsetDateTime.now()
        );
    }

    @Test
    void checkin_ok() throws Exception {
        when(service.checkin(eq(77L), eq(500L), eq("ABC12345"))).thenReturn(fix());
        mvc.perform(post("/merchant/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("ABC12345"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.checkinCode").value("ABC12345"));
    }

    @Test
    void checkin_codeInvalid_returnsBiz() throws Exception {
        when(service.checkin(eq(77L), eq(500L), any()))
            .thenThrow(new BizException("CHECKIN_CODE_INVALID", "签到码错误"));
        mvc.perform(post("/merchant/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("WRONG"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CHECKIN_CODE_INVALID"));
    }

    @Test
    void checkin_tooEarly_returnsBiz() throws Exception {
        when(service.checkin(eq(77L), eq(500L), any()))
            .thenThrow(new BizException("CHECKIN_TOO_EARLY", "签到时间未到"));
        mvc.perform(post("/merchant/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("ABC12345"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CHECKIN_TOO_EARLY"));
    }

    @Test
    void checkin_notOwner_returnsForbidden() throws Exception {
        when(service.checkin(eq(77L), eq(500L), any()))
            .thenThrow(new BizException("FORBIDDEN", "你不是该舞室的认领管理员"));
        mvc.perform(post("/merchant/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("ABC12345"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void checkin_blankCode_returns400() throws Exception {
        mvc.perform(post("/merchant/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest(""))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
