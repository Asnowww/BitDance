package com.bitdance.catalog.controller;

import com.bitdance.catalog.dto.StudioDetail;
import com.bitdance.catalog.dto.UpdateStudioLocationRequest;
import com.bitdance.catalog.service.StudioService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.maps.dto.GeocodeStudioLocationRequest;
import com.bitdance.maps.service.TencentMapService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/studios")
public class AdminStudioLocationController {

    private final StudioService studioService;
    private final TencentMapService tencentMapService;

    public AdminStudioLocationController(StudioService studioService, TencentMapService tencentMapService) {
        this.studioService = studioService;
        this.tencentMapService = tencentMapService;
    }

    @PutMapping("/{id}/location")
    public ApiResponse<StudioDetail> updateLocation(
        @PathVariable Long id,
        @Valid @RequestBody UpdateStudioLocationRequest body
    ) {
        return ApiResponse.ok(studioService.updateLocation(id, body));
    }

    @PostMapping("/{id}/location/geocode")
    public ApiResponse<StudioDetail> geocodeAndUpdateLocation(
        @PathVariable Long id,
        @Valid @RequestBody GeocodeStudioLocationRequest body
    ) {
        // M1 腾讯地图一键标注：服务端解析地址后只写入标准经纬度，WebService Key 不返回给前端。
        var resolved = tencentMapService.geocode(body.address());
        String resolvedAddress = resolved.address() == null ? body.address() : resolved.address();
        return ApiResponse.ok(studioService.updateLocation(id, new UpdateStudioLocationRequest(
            resolvedAddress,
            body.transportInfo(),
            resolved.longitude(),
            resolved.latitude()
        )));
    }
}
