package com.bitdance.maps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// M1 舞室标注请求：管理员提交地址后由后端调用腾讯地图解析坐标并写回舞室。
public record GeocodeStudioLocationRequest(
    @NotBlank @Size(max = 1000) String address,
    @Size(max = 1000) String transportInfo
) {}
