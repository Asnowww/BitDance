package com.bitdance.workshop;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.workshop.controller.WorkshopController;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.CreateOrderRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopBrief;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.dto.WorkshopListResponse;
import com.bitdance.workshop.service.WorkshopService;
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

@WebMvcTest(controllers = WorkshopController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class WorkshopControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean WorkshopService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private WorkshopBrief brief() {
        return new WorkshopBrief(
            10L, 1L, 7L, 1L, 1L, "Yumi Hiphop Workshop",
            null, "海淀区舞星 Studio", new BigDecimal("199.00"), null, "published"
        );
    }

    private SessionDto session(int sold, int cap) {
        OffsetDateTime start = OffsetDateTime.now().plusDays(7);
        return new SessionDto(100L, 10L, "Day1", start, start.plusHours(2), cap, sold, 0, "scheduled");
    }

    private WorkshopDetail detail() {
        return new WorkshopDetail(
            10L, 1L, 7L, 1L, 1L, "Yumi Hiphop Workshop",
            null, "限定档期", "学院路 1 号", "海淀区舞星 Studio",
            new BigDecimal("199.00"), 5, 30, null,
            "published", "approved",
            List.of(session(8, 30)), false
        );
    }

    private OrderDto order(String status, String checkinCode) {
        return new OrderDto(
            500L, "WS17150000001", 10L, 100L, 42L,
            new BigDecimal("199.00"),
            "paid".equals(status) ? new BigDecimal("199.00") : BigDecimal.ZERO,
            status,
            "paid".equals(status) ? "MOCK-abc" : null,
            checkinCode,
            "paid".equals(status) ? OffsetDateTime.now() : null,
            null, null, OffsetDateTime.now()
        );
    }

    // ============ Browse ============

    @Test
    void list_returns() throws Exception {
        when(service.list(eq(1L), eq(null), eq(1), eq(20)))
            .thenReturn(new WorkshopListResponse(List.of(brief()), 1, 20, 1L));
        mvc.perform(get("/public/workshops").param("cityId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].workshopName").value("Yumi Hiphop Workshop"));
    }

    @Test
    void detail_returns() throws Exception {
        when(service.detail(eq(10L), any())).thenReturn(detail());
        mvc.perform(get("/public/workshops/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.workshopName").value("Yumi Hiphop Workshop"))
            .andExpect(jsonPath("$.data.sessions.length()").value(1))
            .andExpect(jsonPath("$.data.sessions[0].capacity").value(30));
    }

    @Test
    void detail_notFound_returnsBiz() throws Exception {
        when(service.detail(eq(999L), any()))
            .thenThrow(new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        mvc.perform(get("/public/workshops/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORKSHOP_NOT_FOUND"));
    }

    // ============ Create order ============

    @Test
    void createOrder_returnsPending() throws Exception {
        when(service.createOrder(eq(42L), any())).thenReturn(order("pending_payment", null));
        mvc.perform(post("/h5/workshop-orders")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateOrderRequest(10L, 100L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("pending_payment"));
    }

    @Test
    void createOrder_missingField_returns400() throws Exception {
        mvc.perform(post("/h5/workshop-orders")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void createOrder_full_returnsBiz() throws Exception {
        when(service.createOrder(eq(42L), any()))
            .thenThrow(new BizException("WORKSHOP_FULL", "场次已满"));
        mvc.perform(post("/h5/workshop-orders")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateOrderRequest(10L, 100L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORKSHOP_FULL"));
    }

    @Test
    void createOrder_signupClosed_returnsBiz() throws Exception {
        when(service.createOrder(eq(42L), any()))
            .thenThrow(new BizException("SIGNUP_CLOSED", "报名已截止"));
        mvc.perform(post("/h5/workshop-orders")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateOrderRequest(10L, 100L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("SIGNUP_CLOSED"));
    }

    @Test
    void createOrder_idempotent_returnsExisting() throws Exception {
        when(service.createOrder(eq(42L), any())).thenReturn(order("paid", "ABC12345"));
        mvc.perform(post("/h5/workshop-orders")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateOrderRequest(10L, 100L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("paid"));
    }

    // ============ Pay ============

    @Test
    void pay_returnsPaidWithCheckinCode() throws Exception {
        when(service.pay(42L, 500L)).thenReturn(order("paid", "ABC12345"));
        mvc.perform(post("/h5/workshop-orders/500/pay").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("paid"))
            .andExpect(jsonPath("$.data.checkinCode").value("ABC12345"));
    }

    @Test
    void pay_stateConflict_returnsBiz() throws Exception {
        when(service.pay(42L, 500L))
            .thenThrow(new BizException("ORDER_STATE_CONFLICT", "订单状态 canceled 不可支付"));
        mvc.perform(post("/h5/workshop-orders/500/pay").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ORDER_STATE_CONFLICT"));
    }

    @Test
    void pay_workshopFullAtReservation_returnsBiz() throws Exception {
        when(service.pay(42L, 500L))
            .thenThrow(new BizException("WORKSHOP_FULL", "场次已满"));
        mvc.perform(post("/h5/workshop-orders/500/pay").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORKSHOP_FULL"));
    }

    @Test
    void pay_otherUser_returnsForbidden() throws Exception {
        when(service.pay(42L, 500L))
            .thenThrow(new BizException("FORBIDDEN", "无权操作他人订单"));
        mvc.perform(post("/h5/workshop-orders/500/pay").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ============ Cancel / Refund ============

    @Test
    void cancel_pending_ok() throws Exception {
        when(service.cancel(42L, 500L)).thenReturn(order("canceled", null));
        mvc.perform(post("/h5/workshop-orders/500/cancel").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("canceled"));
    }

    @Test
    void cancel_paid_returnsBiz() throws Exception {
        when(service.cancel(42L, 500L))
            .thenThrow(new BizException("ORDER_STATE_CONFLICT", "请走退款流程"));
        mvc.perform(post("/h5/workshop-orders/500/cancel").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ORDER_STATE_CONFLICT"));
    }

    @Test
    void refund_ok() throws Exception {
        when(service.refund(eq(42L), eq(500L), any())).thenReturn(order("refunded", null));
        mvc.perform(post("/h5/workshop-orders/500/refund")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("refunded"));
    }

    @Test
    void refund_alreadyCheckedIn_returnsBiz() throws Exception {
        when(service.refund(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("ALREADY_CHECKED_IN", "已签到的订单不可退款"));
        mvc.perform(post("/h5/workshop-orders/500/refund")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ALREADY_CHECKED_IN"));
    }

    // ============ Mine ============

    @Test
    void mine_list() throws Exception {
        when(service.listMyOrders(42L)).thenReturn(List.of(
            order("paid", "ABC12345"),
            order("pending_payment", null)
        ));
        mvc.perform(get("/h5/workshop-orders/mine").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void mine_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(get("/h5/workshop-orders/mine"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    // ============ Checkin ============

    @Test
    void checkin_ok() throws Exception {
        when(service.checkin(eq(42L), eq(500L), any())).thenReturn(order("paid", "ABC12345"));
        mvc.perform(post("/h5/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("ABC12345"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.checkinCode").value("ABC12345"));
    }

    @Test
    void checkin_invalidCode_returnsBiz() throws Exception {
        when(service.checkin(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("CHECKIN_CODE_INVALID", "签到码错误"));
        mvc.perform(post("/h5/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("WRONG"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CHECKIN_CODE_INVALID"));
    }

    @Test
    void checkin_tooEarly_returnsBiz() throws Exception {
        when(service.checkin(eq(42L), eq(500L), any()))
            .thenThrow(new BizException("CHECKIN_TOO_EARLY", "签到时间未到"));
        mvc.perform(post("/h5/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest("ABC12345"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CHECKIN_TOO_EARLY"));
    }

    @Test
    void checkin_blankCode_returns400() throws Exception {
        mvc.perform(post("/h5/workshop-orders/500/checkin")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CheckinRequest(""))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
