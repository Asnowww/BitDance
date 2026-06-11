package com.bitdance.catalog;

import com.bitdance.catalog.controller.AdminStudioLocationController;
import com.bitdance.catalog.dto.StudioDetail;
import com.bitdance.catalog.dto.UpdateStudioLocationRequest;
import com.bitdance.catalog.service.StudioService;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.service.TencentMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminStudioLocationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminStudioLocationControllerTest {

    @Autowired MockMvc mvc;
    @MockBean StudioService studioService;
    @MockBean TencentMapService tencentMapService;
    @MockBean JwtService jwtService;

    @Test
    void geocodeAndUpdateLocation_writesResolvedCoordinates() throws Exception {
        when(tencentMapService.geocode(eq("北京市海淀区测试舞室"))).thenReturn(new MapGeocodeResult(
            "测试舞室",
            "北京市海淀区测试舞室",
            new BigDecimal("39.901"),
            new BigDecimal("116.401"),
            "110108",
            "北京市",
            "北京市",
            "海淀区"
        ));
        when(studioService.updateLocation(eq(101L), argThat(this::matchesResolvedLocation))).thenReturn(new StudioDetail(
            101L,
            "测试舞室",
            "测试",
            "北京市海淀区测试舞室",
            "地铁可达",
            1L,
            null,
            new BigDecimal("39.901"),
            new BigDecimal("116.401"),
            "010-1234",
            "主打街舞",
            null,
            "claimed",
            List.of(1L),
            false
        ));

        // M1 舞室标注测试：地址解析和写库参数分开 mock，确保经纬度顺序不会反。
        mvc.perform(post("/admin/studios/101/location/geocode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"address":"北京市海淀区测试舞室","transportInfo":"地铁可达"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.latitude").value(39.901))
            .andExpect(jsonPath("$.data.longitude").value(116.401));

        verify(studioService).updateLocation(eq(101L), argThat(this::matchesResolvedLocation));
    }

    private boolean matchesResolvedLocation(UpdateStudioLocationRequest req) {
        return req != null
            && new BigDecimal("116.401").compareTo(req.longitude()) == 0
            && new BigDecimal("39.901").compareTo(req.latitude()) == 0
            && "地铁可达".equals(req.transportInfo());
    }
}
