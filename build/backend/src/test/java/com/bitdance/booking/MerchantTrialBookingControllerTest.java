package com.bitdance.booking;

import com.bitdance.booking.controller.MerchantTrialBookingController;
import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.dto.RejectBookingRequest;
import com.bitdance.booking.service.MerchantTrialBookingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MerchantTrialBookingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MerchantTrialBookingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean MerchantTrialBookingService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("77");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("STUDIO_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private BookingDto fixture(String s) {
        return new BookingDto(
            500L, 42L, 101L, 9001L, 1L, s,
            "13800000000", "想试听",
            OffsetDateTime.now(), null, null, null,
            OffsetDateTime.now()
        );
    }

    @Test
    void confirm_ok() throws Exception {
        when(service.confirm(77L, 500L)).thenReturn(fixture("confirmed"));
        mvc.perform(post("/merchant/trial-bookings/500/confirm")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingStatus").value("confirmed"));
    }

    @Test
    void confirm_stateConflict_returnsBiz() throws Exception {
        when(service.confirm(77L, 500L))
            .thenThrow(new BizException("BOOKING_STATE_CONFLICT", "当前状态 confirmed 不可执行该操作"));
        mvc.perform(post("/merchant/trial-bookings/500/confirm")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BOOKING_STATE_CONFLICT"));
    }

    @Test
    void reject_withReason_ok() throws Exception {
        when(service.reject(eq(77L), eq(500L), eq("时间冲突"))).thenReturn(fixture("rejected"));
        mvc.perform(post("/merchant/trial-bookings/500/reject")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new RejectBookingRequest("时间冲突"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingStatus").value("rejected"));
    }

    @Test
    void attend_ok() throws Exception {
        when(service.attend(77L, 500L)).thenReturn(fixture("attended"));
        mvc.perform(post("/merchant/trial-bookings/500/attend")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingStatus").value("attended"));
    }

    @Test
    void noShow_ok() throws Exception {
        when(service.noShow(77L, 500L)).thenReturn(fixture("no_show"));
        mvc.perform(post("/merchant/trial-bookings/500/no-show")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingStatus").value("no_show"));
    }

    @Test
    void confirm_notFound_returnsBiz() throws Exception {
        when(service.confirm(77L, 999L))
            .thenThrow(new BizException("BOOKING_NOT_FOUND", "预约不存在"));
        mvc.perform(post("/merchant/trial-bookings/999/confirm")
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BOOKING_NOT_FOUND"));
    }
}
