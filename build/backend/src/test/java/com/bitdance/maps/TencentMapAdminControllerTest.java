package com.bitdance.maps;

import com.bitdance.maps.controller.TencentMapAdminController;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.dto.MapPlaceListResponse;
import com.bitdance.maps.dto.MapPlaceResult;
import com.bitdance.maps.service.TencentMapService;
import com.bitdance.iam.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TencentMapAdminController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class TencentMapAdminControllerTest {

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
            "海淀区"
        ));

        // M1 腾讯地图接口测试：管理员可用地址换取坐标，真实 Key 不参与单元测试。
        mvc.perform(get("/admin/maps/tencent/geocode").param("address", "北京市海淀区测试舞室"))
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

        // M1 腾讯地图候选测试：地点搜索结果供管理端人工确认后再写入舞室位置。
        mvc.perform(get("/admin/maps/tencent/places")
                .param("keyword", "舞室")
                .param("city", "北京"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].title").value("舞室 A"))
            .andExpect(jsonPath("$.data.total").value(1));
    }
}
