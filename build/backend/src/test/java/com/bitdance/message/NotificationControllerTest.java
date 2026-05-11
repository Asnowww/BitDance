package com.bitdance.message;

import com.bitdance.iam.jwt.JwtService;
import com.bitdance.message.controller.NotificationController;
import com.bitdance.message.dto.NotificationDto;
import com.bitdance.message.dto.NotificationListResponse;
import com.bitdance.message.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class NotificationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean NotificationService notificationService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("7");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void list_returnsItems() throws Exception {
        when(notificationService.list(eq(7L), any(), eq(1), eq(20)))
            .thenReturn(new NotificationListResponse(
                List.of(new NotificationDto(
                    1L, "system", "system", "欢迎", "Welcome",
                    null, null, false, null, OffsetDateTime.now()
                )),
                1, 20, 1L, 1L
            ));
        mvc.perform(get("/h5/messages").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.list[0].title").value("欢迎"))
            .andExpect(jsonPath("$.data.unread").value(1));
    }

    @Test
    void markRead_returnsOk() throws Exception {
        doNothing().when(notificationService).markRead(7L, 99L);
        mvc.perform(post("/h5/messages/99/read").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void markAllRead_returnsAffected() throws Exception {
        when(notificationService.markAllRead(7L)).thenReturn(3);
        mvc.perform(post("/h5/messages/read-all").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.affected").value(3));
    }

    @Test
    void list_withoutToken_returns401Biz() throws Exception {
        mvc.perform(get("/h5/messages"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
