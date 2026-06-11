package com.bitdance.admin.controller;

import com.bitdance.admin.dto.GrantUserRoleRequest;
import com.bitdance.admin.dto.UserRoleBindingDto;
import com.bitdance.admin.service.AdminUserRoleService;
import com.bitdance.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users/{userId}/roles")
public class AdminUserRoleController {

    private final AdminUserRoleService service;

    public AdminUserRoleController(AdminUserRoleService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<UserRoleBindingDto>> list(@PathVariable Long userId) {
        return ApiResponse.ok(service.list(userId));
    }

    @PostMapping
    public ApiResponse<UserRoleBindingDto> grant(
        @PathVariable Long userId,
        @Valid @RequestBody GrantUserRoleRequest body
    ) {
        return ApiResponse.ok(service.grant(userId, body.role()));
    }

    @DeleteMapping("/{role}")
    public ApiResponse<UserRoleBindingDto> revoke(
        @PathVariable Long userId,
        @PathVariable String role
    ) {
        return ApiResponse.ok(service.revoke(userId, role));
    }
}
