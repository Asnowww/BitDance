package com.bitdance.booking;

import com.bitdance.booking.controller.TrialBookingController;
import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.dto.CancelBookingRequest;
import com.bitdance.booking.dto.CreateBookingRequest;
import com.bitdance.booking.service.TrialBookingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrialBookingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class TrialBookingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean TrialBookingService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private BookingDto fixture(String status) {
        return new BookingDto(
            500L, 42L, 101L, 9001L, 1L, status,
            "13800000000", "周末来试听",
            null, null, null, null, OffsetDateTime.now()
        );
    }

    @Test
    void create_pendingOk() throws Exception {
        when(service.create(eq(42L), any(CreateBookingRequest.class)))
            .thenReturn(fixture("pending"));
        var body = new CreateBookingRequest(101L, 9001L, "13800000000", "周末来试听");
        mvc.perform(post("/h5/trial-bookings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.bookingStatus").value("pending"));
    }

    @Test
    void create_invalidPhone_returns400() throws Exception {
        var body = new CreateBookingRequest(101L, null, "not-a-phone", null);
        mvc.perform(post("/h5/trial-bookings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_missingCourseId_returns400() throws Exception {
        mvc.perform(post("/h5/trial-bookings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_duplicated_returnsBiz() throws Exception {
        when(service.create(eq(42L), any()))
            .thenThrow(new BizException("BOOKING_DUPLICATED", "已有未完结的同课程预约"));
        var body = new CreateBookingRequest(101L, 9001L, "13800000000", null);
        mvc.perform(post("/h5/trial-bookings")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BOOKING_DUPLICATED"));
    }

    @Test
    void cancel_ok() throws Exception {
        when(service.cancel(eq(42L), eq(500L), eq("时间冲突")))
            .thenReturn(fixture("canceled"));
        mvc.perform(post("/h5/trial-bookings/500/cancel")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CancelBookingRequest("时间冲突"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingStatus").value("canceled"));
    }

    @Test
    void cancel_stateConflict_returns400() throws Exception {
        when(service.cancel(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("BOOKING_STATE_CONFLICT", "当前状态 attended 不可取消"));
        mvc.perform(post("/h5/trial-bookings/500/cancel")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BOOKING_STATE_CONFLICT"));
    }

    @Test
    void cancel_otherUser_returnsForbidden() throws Exception {
        when(service.cancel(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("FORBIDDEN", "无权操作他人预约"));
        mvc.perform(post("/h5/trial-bookings/500/cancel")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void listMine_returnsItems() throws Exception {
        when(service.listMine(42L)).thenReturn(List.of(
            fixture("pending"),
            fixture("confirmed")
        ));
        mvc.perform(get("/h5/trial-bookings").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void list_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/trial-bookings"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
