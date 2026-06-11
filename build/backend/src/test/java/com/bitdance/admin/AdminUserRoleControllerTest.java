package com.bitdance.admin;

import com.bitdance.admin.controller.AdminUserRoleController;
import com.bitdance.admin.dto.GrantUserRoleRequest;
import com.bitdance.admin.dto.UserRoleBindingDto;
import com.bitdance.admin.service.AdminUserRoleService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserRoleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminUserRoleControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean AdminUserRoleService service;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("7");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("PLATFORM_ADMIN"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void list_ok() throws Exception {
        when(service.list(42L)).thenReturn(List.of(new UserRoleBindingDto(1L, 42L, "USER", "ACTIVE")));
        mvc.perform(get("/admin/users/42/roles").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].role").value("USER"));
    }

    @Test
    void grant_ok() throws Exception {
        when(service.grant(42L, "COACH")).thenReturn(new UserRoleBindingDto(2L, 42L, "COACH", "ACTIVE"));
        mvc.perform(post("/admin/users/42/roles")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new GrantUserRoleRequest("COACH"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("COACH"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void revoke_ok() throws Exception {
        when(service.revoke(42L, "COACH")).thenReturn(new UserRoleBindingDto(2L, 42L, "COACH", "INACTIVE"));
        mvc.perform(delete("/admin/users/42/roles/COACH").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void grant_invalidRole_returnsBiz() throws Exception {
        when(service.grant(42L, "OWNER")).thenThrow(new BizException("INVALID_ROLE", "Invalid role: OWNER"));
        mvc.perform(post("/admin/users/42/roles")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new GrantUserRoleRequest("OWNER"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ROLE"));
    }
}
