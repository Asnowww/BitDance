package com.bitdance.catalog;

import com.bitdance.catalog.controller.StudioController;
import com.bitdance.catalog.dto.StudioCard;
import com.bitdance.catalog.dto.StudioDetail;
import com.bitdance.catalog.dto.StudioListResponse;
import com.bitdance.catalog.service.StudioService;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudioController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class StudioControllerTest {

    @Autowired MockMvc mvc;
    @MockBean StudioService studioService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("11");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    @Test
    void nearby_anonymous_returnsList() throws Exception {
        when(studioService.searchNearby(
            eq(1L), eq(39.9), eq(116.4), eq(5.0), eq(null), eq(null),
            eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
            eq(1), eq(20), eq(null)
        )).thenReturn(new StudioListResponse(List.of(
            new StudioCard(101L, "舞星 Studio", "海淀区学院路 1 号", 1L, null, null,
                new BigDecimal("0.8"), new BigDecimal("39.901"), new BigDecimal("116.401"), false)
        ), 1, 20, 1));

        mvc.perform(get("/public/studios/nearby")
                .param("cityId", "1")
                .param("latitude", "39.9")
                .param("longitude", "116.4")
                .param("distanceKm", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.list[0].id").value(101))
            .andExpect(jsonPath("$.data.list[0].name").value("舞星 Studio"))
            .andExpect(jsonPath("$.data.list[0].distanceKm").value(0.8))
            .andExpect(jsonPath("$.data.list[0].favored").value(false))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void nearby_authenticated_passesUserIdForFavored() throws Exception {
        when(studioService.searchNearby(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            eq(1), eq(20), eq(11L)
        )).thenReturn(new StudioListResponse(List.of(
            new StudioCard(202L, "灵动 Studio", "朝阳区", 1L, null, null,
                null, null, null, true)
        ), 1, 20, 1));

        mvc.perform(get("/public/studios/nearby").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].favored").value(true));
    }

    @Test
    void nearby_passesDatabaseBackedFilters() throws Exception {
        when(studioService.searchNearby(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            eq(1), eq(20), eq(null)
        )).thenReturn(new StudioListResponse(List.of(), 1, 20, 0));

        mvc.perform(get("/public/studios/nearby")
                .param("danceStyleId", "2")
                .param("minPrice", "80")
                .param("maxPrice", "180")
                .param("timeSlot", "evening")
                .param("trialAvailable", "true")
                .param("zeroBasicFriendly", "true")
                .param("nearMetro", "true"))
            .andExpect(status().isOk());

        verify(studioService).searchNearby(
            eq(null), eq(null), eq(null), eq(null), eq(null), eq(2L),
            eq(new BigDecimal("80")), eq(new BigDecimal("180")), eq("evening"),
            eq(true), eq(true), eq(true), eq(1), eq(20), eq(null)
        );
    }

    @Test
    void detail_returns() throws Exception {
        when(studioService.detail(eq(101L), eq(null))).thenReturn(new StudioDetail(
            101L, "舞星 Studio", "舞星", "海淀区学院路 1 号", "地铁 13 号线",
            1L, null, new BigDecimal("39.901"), new BigDecimal("116.401"),
            "010-1234", "主打街舞", null, "claimed",
            List.of(1L, 2L), false
        ));
        mvc.perform(get("/public/studios/101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("舞星 Studio"))
            .andExpect(jsonPath("$.data.danceStyleIds.length()").value(2));
    }

    @Test
    void detail_notFound_returns400Biz() throws Exception {
        when(studioService.detail(eq(999L), any()))
            .thenThrow(new BizException("STUDIO_NOT_FOUND", "舞室不存在"));
        mvc.perform(get("/public/studios/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("STUDIO_NOT_FOUND"));
    }
}
