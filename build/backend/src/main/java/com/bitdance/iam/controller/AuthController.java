package com.bitdance.iam.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.dto.LoginRequest;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.PasswordLoginRequest;
import com.bitdance.iam.dto.SendSmsRequest;
import com.bitdance.iam.dto.WechatAuthorizeUrlResponse;
import com.bitdance.iam.dto.WechatLoginRequest;
import com.bitdance.iam.service.AuthService;
import com.bitdance.iam.service.WechatOAuthClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final WechatOAuthClient wechatOAuthClient;

    public AuthController(AuthService authService, WechatOAuthClient wechatOAuthClient) {
        this.authService = authService;
        this.wechatOAuthClient = wechatOAuthClient;
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

    @PostMapping("/login/password")
    public ApiResponse<LoginResponse> loginPassword(@Valid @RequestBody PasswordLoginRequest body) {
        return ApiResponse.ok(authService.loginWithPassword(body.phone(), body.password()));
    }

    @PostMapping("/login/wechat")
    public ApiResponse<LoginResponse> loginWechat(@Valid @RequestBody WechatLoginRequest body) {
        return ApiResponse.ok(authService.loginWithWechat(body.code()));
    }

    @GetMapping("/wechat/authorize-url")
    public ApiResponse<WechatAuthorizeUrlResponse> wechatAuthorizeUrl(
        @RequestParam(defaultValue = "") String state
    ) {
        return ApiResponse.ok(new WechatAuthorizeUrlResponse(wechatOAuthClient.authorizeUrl(state)));
    }

    @GetMapping("/wechat/callback")
    public RedirectView wechatCallback(
        @RequestParam String code,
        @RequestParam(defaultValue = "") String state
    ) {
        return new RedirectView(wechatOAuthClient.frontendCallbackUrl(code, state));
    }
}
