package com.bitdance.iam.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.dto.LoginRequest;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.SendSmsRequest;
import com.bitdance.iam.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sms/send")
    public ApiResponse<Map<String, Object>> sendSms(@Valid @RequestBody SendSmsRequest body) {
        authService.sendSmsCode(body.phone());
        return ApiResponse.ok(Map.of("sent", true, "expiresIn", 60));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest body) {
        return ApiResponse.ok(authService.loginWithSms(body.phone(), body.code()));
    }
}
