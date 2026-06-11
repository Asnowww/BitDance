package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WechatOAuthClient {

    private static final String AUTHORIZE_ENDPOINT = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static final String QRCONNECT_ENDPOINT = "https://open.weixin.qq.com/connect/qrconnect";
    private static final String TOKEN_ENDPOINT = "https://api.weixin.qq.com/sns/oauth2/access_token";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String appId;
    private final String appSecret;
    private final String redirectUri;
    private final String scope;
    private final String frontendCallbackUri;

    public WechatOAuthClient(
        ObjectMapper objectMapper,
        @Value("${bitdance.wechat.app-id:}") String appId,
        @Value("${bitdance.wechat.app-secret:}") String appSecret,
        @Value("${bitdance.wechat.redirect-uri:}") String redirectUri,
        @Value("${bitdance.wechat.scope:snsapi_userinfo}") String scope,
        @Value("${bitdance.wechat.frontend-callback-uri:http://localhost:5173/#/login}") String frontendCallbackUri
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
        this.appId = appId;
        this.appSecret = appSecret;
        this.redirectUri = redirectUri;
        this.scope = StringUtils.hasText(scope) ? scope : "snsapi_userinfo";
        this.frontendCallbackUri = frontendCallbackUri;
    }

    public String authorizeUrl(String state) {
        validateConfig();
        String endpoint = "snsapi_login".equals(scope) ? QRCONNECT_ENDPOINT : AUTHORIZE_ENDPOINT;
        return UriComponentsBuilder.fromUriString(endpoint)
            .queryParam("appid", appId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", scope)
            .queryParam("state", state)
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString() + "#wechat_redirect";
    }

    public WechatIdentity exchangeCode(String code) {
        validateConfig();
        if (!StringUtils.hasText(code)) {
            throw new BizException("WECHAT_CODE_REQUIRED", "缺少微信授权 code");
        }
        URI uri = UriComponentsBuilder.fromUriString(TOKEN_ENDPOINT)
            .queryParam("appid", appId)
            .queryParam("secret", appSecret)
            .queryParam("code", code)
            .queryParam("grant_type", "authorization_code")
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUri();
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode body = objectMapper.readTree(response.body());
            if (body.has("errcode")) {
                throw new BizException(
                    "WECHAT_OAUTH_FAILED",
                    "微信授权失败：" + body.path("errmsg").asText("unknown error")
                );
            }
            String openId = body.path("openid").asText("");
            String unionId = body.path("unionid").asText("");
            if (!StringUtils.hasText(openId)) {
                throw new BizException("WECHAT_OAUTH_FAILED", "微信授权失败：未返回 openid");
            }
            return new WechatIdentity(openId, StringUtils.hasText(unionId) ? unionId : null);
        } catch (IOException ex) {
            throw new BizException("WECHAT_OAUTH_FAILED", "微信授权服务调用失败：" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BizException("WECHAT_OAUTH_FAILED", "微信授权服务调用被中断");
        }
    }

    public String frontendCallbackUrl(String code, String state) {
        String separator = frontendCallbackUri.contains("?") ? "&" : "?";
        return frontendCallbackUri
            + separator
            + "wechatCode=" + encode(code)
            + "&wechatState=" + encode(state == null ? "" : state);
    }

    private void validateConfig() {
        List<String> missing = new ArrayList<>();
        require(appId, "BITDANCE_WECHAT_APP_ID", missing);
        require(appSecret, "BITDANCE_WECHAT_APP_SECRET", missing);
        require(redirectUri, "BITDANCE_WECHAT_REDIRECT_URI", missing);
        if (!missing.isEmpty()) {
            throw new BizException("WECHAT_NOT_CONFIGURED", "微信授权配置不完整：" + String.join(", ", missing));
        }
    }

    private void require(String value, String name, List<String> missing) {
        if (!StringUtils.hasText(value) || "/".equals(value.trim())) {
            missing.add(name);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record WechatIdentity(String openId, String unionId) {
    }
}
