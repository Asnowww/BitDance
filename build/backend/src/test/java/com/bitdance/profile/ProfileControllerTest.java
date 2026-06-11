package com.bitdance.profile;

import com.bitdance.iam.jwt.JwtService;
import com.bitdance.profile.controller.ProfileController;
import com.bitdance.profile.dto.PrivacyDto;
import com.bitdance.profile.dto.ProfileResponse;
import com.bitdance.profile.dto.StylePreferenceDto;
import com.bitdance.profile.dto.UpdateProfileRequest;
import com.bitdance.profile.service.ProfileService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ProfileControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean ProfileService profileService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private ProfileResponse fixture() {
        return new ProfileResponse(
            42L, "舞者0042", null, "unknown",
            LocalDate.of(2000, 1, 1), "热爱街舞", null, "intermediate", "完成一支 Routine",
            List.of("USER"),
            List.of(new StylePreferenceDto(1L, "Hiphop", "intermediate", true)),
            new PrivacyDto("public", "followers", "public", "public")
        );
    }

    @Test
    void get_returnsProfile() throws Exception {
        when(profileService.get(42L)).thenReturn(fixture());
        mvc.perform(get("/h5/profile").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.userId").value(42))
            .andExpect(jsonPath("$.data.styles[0].name").value("Hiphop"))
            .andExpect(jsonPath("$.data.privacy.profileVisibility").value("public"));
    }

    @Test
    void update_returnsUpdatedProfile() throws Exception {
        when(profileService.update(eq(42L), any(UpdateProfileRequest.class)))
            .thenReturn(fixture());
        UpdateProfileRequest body = new UpdateProfileRequest(
            "新昵称", null, "male", LocalDate.of(1999, 5, 1), "bio", null, "advanced", "目标",
            List.of(new StylePreferenceDto(1L, null, "intermediate", true)),
            new PrivacyDto("public", "followers", "public", "public")
        );
        mvc.perform(put("/h5/profile")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(42));
    }

    @Test
    void update_oversizeBio_returns400() throws Exception {
        String bigBio = "x".repeat(1001);
        UpdateProfileRequest body = new UpdateProfileRequest(
            null, null, null, null, bigBio, null, null, null, null, null
        );
        mvc.perform(put("/h5/profile")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void get_withoutToken_returns401Style() throws Exception {
        // 未带 Authorization → CurrentUser.getId 抛 BizException(UNAUTHORIZED)
        mvc.perform(get("/h5/profile"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
