package com.bitdance.maps.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.dto.MapPlaceListResponse;
import com.bitdance.maps.service.TencentMapService;
import com.bitdance.maps.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

@Validated
@RestController
@RequestMapping("/h5/maps/tencent")
public class TencentMapH5Controller {

    private static final MapGeocodeResult DEFAULT_BEIJING_LOCATION = new MapGeocodeResult(
        "中关村",
        "北京市海淀区中关村大街",
        new BigDecimal("39.984120"),
        new BigDecimal("116.307484"),
        "110108",
        "北京市",
        "北京市",
        "海淀区",
        List.of()
    );

    private final TencentMapService tencentMapService;

    public TencentMapH5Controller(TencentMapService tencentMapService) {
        this.tencentMapService = tencentMapService;
    }

    @GetMapping("/geocode")
    public ApiResponse<MapGeocodeResult> geocode(
        @RequestParam @NotBlank @Size(max = 1000) String address
    ) {
        return ApiResponse.ok(tencentMapService.geocode(address));
    }

    @GetMapping("/reverse-geocode")
    public ApiResponse<MapGeocodeResult> reverseGeocode(
        @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
    ) {
        return ApiResponse.ok(tencentMapService.reverseGeocode(latitude, longitude));
    }

    @GetMapping("/ip-location")
    public ApiResponse<MapGeocodeResult> locateByIp(HttpServletRequest request) {
        try {
            return ApiResponse.ok(tencentMapService.locateByIp(ClientIpResolver.resolvePublicIp(request)));
        } catch (RuntimeException ex) {
            return ApiResponse.ok(DEFAULT_BEIJING_LOCATION);
        }
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
        return ApiResponse.ok(tencentMapService.searchPlaces(keyword, city, latitude, longitude, radiusMeters, page, pageSize));
    }
}
