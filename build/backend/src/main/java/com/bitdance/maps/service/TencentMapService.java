package com.bitdance.maps.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.maps.dto.MapGeocodeResult;
import com.bitdance.maps.dto.MapPlaceListResponse;
import com.bitdance.maps.dto.MapPlaceResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TencentMapService {

    private final RestClient restClient;
    private final String key;

    public TencentMapService(
        @Value("${bitdance.maps.tencent.base-url:https://apis.map.qq.com}") String baseUrl,
        @Value("${bitdance.maps.tencent.key:}") String key
    ) {
        // M1 腾讯地图服务端调用：只保存 WebService 基础地址和后端 Key，避免前端暴露服务端配额。
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.key = key;
    }

    public MapGeocodeResult geocode(String address) {
        assertConfigured();
        try {
            JsonNode root = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ws/geocoder/v1/")
                    .queryParam("address", address)
                    .queryParam("key", key)
                    .build())
                .retrieve()
                .body(JsonNode.class);
            JsonNode result = requireSuccess(root).path("result");
            JsonNode location = result.path("location");
            JsonNode components = result.path("address_components");
            BigDecimal latitude = decimal(location, "lat");
            BigDecimal longitude = decimal(location, "lng");
            if (latitude == null || longitude == null) {
                // M1 坐标写回前校验：腾讯未返回坐标时不更新舞室，避免生成不可导航的点位。
                throw new BizException("TENCENT_MAP_LOCATION_MISSING", "腾讯地图未返回有效经纬度");
            }
            return new MapGeocodeResult(
                text(result, "title", address),
                text(result, "address", address),
                latitude,
                longitude,
                text(components, "adcode", null),
                text(components, "province", null),
                text(components, "city", null),
                text(components, "district", null)
            );
        } catch (RestClientException ex) {
            throw new BizException("TENCENT_MAP_UNAVAILABLE", "腾讯地图服务暂不可用");
        }
    }

    public MapGeocodeResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        assertConfigured();
        try {
            JsonNode root = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ws/geocoder/v1/")
                    .queryParam("location", "%s,%s".formatted(latitude, longitude))
                    .queryParam("get_poi", 1)
                    .queryParam("key", key)
                    .build())
                .retrieve()
                .body(JsonNode.class);
            JsonNode result = requireSuccess(root).path("result");
            JsonNode components = result.path("address_component");
            JsonNode formatted = result.path("formatted_addresses");
            JsonNode firstPoi = result.path("pois").isArray() && !result.path("pois").isEmpty()
                ? result.path("pois").get(0)
                : null;
            String title = text(formatted, "recommend", null);
            if (!StringUtils.hasText(title)) title = text(firstPoi, "title", null);
            if (!StringUtils.hasText(title)) title = text(result, "address", "当前位置");
            return new MapGeocodeResult(
                title,
                text(result, "address", title),
                latitude,
                longitude,
                text(components, "adcode", null),
                text(components, "province", null),
                text(components, "city", null),
                text(components, "district", null)
            );
        } catch (RestClientException ex) {
            throw new BizException("TENCENT_MAP_UNAVAILABLE", "腾讯地图服务暂不可用");
        }
    }

    public MapPlaceListResponse searchPlaces(
        String keyword,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        int page,
        int pageSize
    ) {
        assertConfigured();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 20);
        String boundary = boundary(city, latitude, longitude, radiusMeters);
        try {
            JsonNode root = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ws/place/v1/search")
                    .queryParam("keyword", keyword)
                    .queryParam("boundary", boundary)
                    .queryParam("page_index", safePage)
                    .queryParam("page_size", safeSize)
                    .queryParam("key", key)
                    .build())
                .retrieve()
                .body(JsonNode.class);
            JsonNode success = requireSuccess(root);
            List<MapPlaceResult> list = new ArrayList<>();
            success.path("data").forEach(item -> {
                JsonNode location = item.path("location");
                JsonNode adInfo = item.path("ad_info");
                list.add(new MapPlaceResult(
                    text(item, "id", null),
                    text(item, "title", null),
                    text(item, "address", null),
                    text(item, "category", null),
                    decimal(location, "lat"),
                    decimal(location, "lng"),
                    text(item, "tel", null),
                    text(adInfo, "adcode", null)
                ));
            });
            return new MapPlaceListResponse(list, safePage, safeSize, success.path("count").isNumber() ? success.path("count").asInt() : null);
        } catch (RestClientException ex) {
            throw new BizException("TENCENT_MAP_UNAVAILABLE", "腾讯地图服务暂不可用");
        }
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(key)) {
            // M1 腾讯地图 Key 缺失时在调用阶段提示，确保本地无 Key 也能启动后端。
            throw new BizException("TENCENT_MAP_KEY_MISSING", "请先配置腾讯地图 WebService Key");
        }
    }

    private String boundary(String city, BigDecimal latitude, BigDecimal longitude, Integer radiusMeters) {
        if (latitude != null && longitude != null) {
            int radius = radiusMeters == null ? 5000 : Math.min(Math.max(radiusMeters, 100), 20000);
            return "nearby(%s,%s,%d,1)".formatted(latitude, longitude, radius);
        }
        return "region(%s,1)".formatted(StringUtils.hasText(city) ? city.trim() : "北京");
    }

    private JsonNode requireSuccess(JsonNode root) {
        if (root == null) {
            throw new BizException("TENCENT_MAP_EMPTY_RESPONSE", "腾讯地图返回为空");
        }
        int status = root.path("status").asInt(-1);
        if (status != 0) {
            throw new BizException("TENCENT_MAP_ERROR", root.path("message").asText("腾讯地图接口返回异常"));
        }
        return root;
    }

    private String text(JsonNode node, String field, String fallback) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return fallback;
        }
        String value = node.path(field).asText();
        return StringUtils.hasText(value) ? value : fallback;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return null;
        }
        return BigDecimal.valueOf(node.path(field).asDouble());
    }
}
