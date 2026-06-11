package com.bitdance.iam;

import com.bitdance.iam.controller.AuthController;
import com.bitdance.iam.dto.LoginRequest;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.SendSmsRequest;
import com.bitdance.iam.dto.UserSummary;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.iam.service.AuthService;
import com.bitdance.iam.service.WechatOAuthClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean AuthService authService;
    @MockBean WechatOAuthClient wechatOAuthClient;
    @MockBean JwtService jwtService;

    @Test
    void sendSms_invalidPhone_returns400() throws Exception {
        mvc.perform(post("/auth/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SendSmsRequest("not-a-phone"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void sendSms_ok() throws Exception {
        doNothing().when(authService).sendSmsCode(eq("13800000000"));
        mvc.perform(post("/auth/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SendSmsRequest("13800000000"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.sent").value(true));
    }

    @Test
    void login_missingCode_returns400() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginRequest("13800000000", ""))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_ok_returnsToken() throws Exception {
        when(authService.loginWithSms(eq("13800000000"), eq("123456")))
            .thenReturn(new LoginResponse(
                "mock-token",
                new UserSummary(1L, "13800000000", "舞者0000", null, List.of("USER"))
            ));
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginRequest("13800000000", "123456"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").value("mock-token"))
            .andExpect(jsonPath("$.data.user.id").value(1));
    }
}
