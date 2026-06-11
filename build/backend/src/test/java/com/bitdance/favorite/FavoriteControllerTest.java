package com.bitdance.favorite;

import com.bitdance.favorite.controller.FavoriteController;
import com.bitdance.favorite.dto.FavoriteDto;
import com.bitdance.favorite.dto.ToggleFavoriteRequest;
import com.bitdance.favorite.service.FavoriteService;
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

@WebMvcTest(controllers = FavoriteController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class FavoriteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean FavoriteService favoriteService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("11");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void toggle_add_returnsTrue() throws Exception {
        when(favoriteService.toggle(11L, "studio", 101L)).thenReturn(true);
        mvc.perform(post("/h5/favorites")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ToggleFavoriteRequest("studio", 101L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.favored").value(true));
    }

    @Test
    void toggle_remove_returnsFalse() throws Exception {
        when(favoriteService.toggle(11L, "studio", 101L)).thenReturn(false);
        mvc.perform(post("/h5/favorites")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ToggleFavoriteRequest("studio", 101L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.favored").value(false));
    }

    @Test
    void toggle_missingFields_returns400() throws Exception {
        mvc.perform(post("/h5/favorites")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void list_byType_returnsItems() throws Exception {
        when(favoriteService.list(11L, "studio")).thenReturn(List.of(
            new FavoriteDto(1L, "studio", 101L, OffsetDateTime.now(), null),
            new FavoriteDto(2L, "studio", 102L, OffsetDateTime.now(), null)
        ));
        mvc.perform(get("/h5/favorites")
                .header("Authorization", "Bearer fake")
                .param("targetType", "studio"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].targetId").value(101));
    }

    @Test
    void check_returnsFavored() throws Exception {
        when(favoriteService.check(11L, "studio", 101L)).thenReturn(true);
        mvc.perform(get("/h5/favorites/check")
                .header("Authorization", "Bearer fake")
                .param("targetType", "studio")
                .param("targetId", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.favored").value(true));
    }

    @Test
    void toggle_withoutToken_returns400Unauthorized() throws Exception {
        mvc.perform(post("/h5/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ToggleFavoriteRequest("studio", 101L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
