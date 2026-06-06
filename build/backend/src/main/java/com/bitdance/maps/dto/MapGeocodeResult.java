package com.bitdance.maps.dto;

import java.math.BigDecimal;

// M1 腾讯地图地理编码结果：只返回业务需要的地址和标准经纬度，不透出服务端 Key。
public record MapGeocodeResult(
    String title,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    String adcode,
    String province,
    String city,
    String district
) {}
