package com.bitdance.maps.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.dto.MapPlaceListResponse;
import com.bitdance.maps.service.TencentMapService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/admin/maps/tencent")
public class TencentMapAdminController {

    private final TencentMapService tencentMapService;

    public TencentMapAdminController(TencentMapService tencentMapService) {
        this.tencentMapService = tencentMapService;
    }

    @GetMapping("/geocode")
    public ApiResponse<MapGeocodeResult> geocode(
        @RequestParam @NotBlank @Size(max = 1000) String address
    ) {
        // M1 管理端地址解析：管理员只传业务地址，腾讯 WebService Key 保留在后端本地配置。
        return ApiResponse.ok(tencentMapService.geocode(address));
    }

    @GetMapping("/places")
    public ApiResponse<MapPlaceListResponse> searchPlaces(
        @RequestParam @NotBlank @Size(max = 100) String keyword,
        @RequestParam(defaultValue = "北京") @Size(max = 40) String city,
        @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @RequestParam(required = false) @Min(100) @Max(20000) Integer radiusMeters,
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) int pageSize
    ) {
        // M1 管理端地点候选：支持城市检索和附近检索，便于手动标注舞室前先核对腾讯地点数据。
        return ApiResponse.ok(tencentMapService.searchPlaces(keyword, city, latitude, longitude, radiusMeters, page, pageSize));
    }
}
