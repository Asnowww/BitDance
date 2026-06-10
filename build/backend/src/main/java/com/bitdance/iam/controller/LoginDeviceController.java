package com.bitdance.iam.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.dto.LoginDeviceDto;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.iam.service.LoginDeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/h5/login-devices")
public class LoginDeviceController {

    private final LoginDeviceService service;

    public LoginDeviceController(LoginDeviceService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<LoginDeviceDto>> list() {
        return ApiResponse.ok(service.list(CurrentUser.getId()));
    }

    @PostMapping("/{id}/trust")
    public ApiResponse<LoginDeviceDto> trust(@PathVariable Long id) {
        return ApiResponse.ok(service.trust(CurrentUser.getId(), id));
    }
}
