package com.bitdance.maps;

import com.bitdance.iam.jwt.JwtService;
import com.bitdance.maps.controller.TencentMapH5Controller;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.dto.MapPlaceListResponse;
import com.bitdance.maps.dto.MapPlaceResult;
import com.bitdance.maps.service.TencentMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TencentMapH5Controller.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class TencentMapH5ControllerTest {

    @Autowired MockMvc mvc;
    @MockBean TencentMapService tencentMapService;
    @MockBean JwtService jwtService;

    @Test
    void geocode_returnsTencentCoordinates() throws Exception {
        when(tencentMapService.geocode(eq("北京市海淀区测试舞室"))).thenReturn(new MapGeocodeResult(
            "测试舞室",
            "北京市海淀区测试舞室",
            new BigDecimal("39.901"),
            new BigDecimal("116.401"),
            "110108",
            "北京市",
            "北京市",
            "海淀区",
            List.of()
        ));

        mvc.perform(get("/h5/maps/tencent/geocode").param("address", "北京市海淀区测试舞室"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("测试舞室"))
            .andExpect(jsonPath("$.data.latitude").value(39.901))
            .andExpect(jsonPath("$.data.longitude").value(116.401));
    }

    @Test
    void places_returnsTencentCandidates() throws Exception {
        when(tencentMapService.searchPlaces(
            eq("舞室"), eq("北京"), eq(null), eq(null), eq(null), eq(1), eq(10)
        )).thenReturn(new MapPlaceListResponse(List.of(
            new MapPlaceResult("poi-1", "舞室 A", "北京市朝阳区", "教育培训", new BigDecimal("39.92"), new BigDecimal("116.45"), "010", "110105")
        ), 1, 10, 1));

        mvc.perform(get("/h5/maps/tencent/places")
                .param("keyword", "舞室")
                .param("city", "北京"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].title").value("舞室 A"))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void reverseGeocode_returnsAddressAndPois() throws Exception {
        when(tencentMapService.reverseGeocode(eq(new BigDecimal("39.85362")), eq(new BigDecimal("116.67618"))))
            .thenReturn(new MapGeocodeResult(
                "北京市通州区附近",
                "北京市通州区当前位置附近",
                new BigDecimal("39.85362"),
                new BigDecimal("116.67618"),
                "110112",
                "北京市",
                "北京市",
                "通州区",
                List.of(
                    new MapPlaceResult("poi-1", "北京环球度假区", "北京市通州区京哈高速与东六环路交会处西北角", "旅游景点", new BigDecimal("39.85506"), new BigDecimal("116.67595"), "", "110112")
                )
            ));

        mvc.perform(get("/h5/maps/tencent/reverse-geocode")
                .param("latitude", "39.85362")
                .param("longitude", "116.67618"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("北京市通州区附近"))
            .andExpect(jsonPath("$.data.latitude").value(39.85362))
            .andExpect(jsonPath("$.data.pois[0].title").value("北京环球度假区"));
    }

    @Test
    void ipLocation_returnsApproximateCoordinates() throws Exception {
        when(tencentMapService.locateByIp(isNull()))
            .thenReturn(new MapGeocodeResult(
                "海淀区",
                "北京市海淀区",
                new BigDecimal("39.984120"),
                new BigDecimal("116.307480"),
                "110108",
                "北京市",
                "北京市",
                "海淀区",
                List.of()
            ));

        mvc.perform(get("/h5/maps/tencent/ip-location"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("海淀区"))
            .andExpect(jsonPath("$.data.latitude").value(39.98412))
            .andExpect(jsonPath("$.data.longitude").value(116.30748));
    }
}
