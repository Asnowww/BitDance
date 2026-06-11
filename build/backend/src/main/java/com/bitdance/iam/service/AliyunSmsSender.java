package com.bitdance.iam.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.bitdance.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AliyunSmsSender implements SmsSender {

    private final ObjectMapper objectMapper;
    private final String provider;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final String templateParamName;
    private final String templateMinParamName;
    private final long ttlMinutes;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public AliyunSmsSender(
        ObjectMapper objectMapper,
        @Value("${bitdance.sms.provider:aliyun}") String provider,
        @Value("${bitdance.sms.aliyun.endpoint:dysmsapi.aliyuncs.com}") String endpoint,
        @Value("${bitdance.sms.aliyun.access-key-id:}") String accessKeyId,
        @Value("${bitdance.sms.aliyun.access-key-secret:}") String accessKeySecret,
        @Value("${bitdance.sms.aliyun.sign-name:}") String signName,
        @Value("${bitdance.sms.aliyun.template-code:}") String templateCode,
        @Value("${bitdance.sms.aliyun.template-param-name:code}") String templateParamName,
        @Value("${bitdance.sms.aliyun.template-min-param-name:min}") String templateMinParamName,
        @Value("${bitdance.sms.ttl-minutes:5}") long ttlMinutes,
        @Value("${bitdance.sms.aliyun.connect-timeout-ms:5000}") int connectTimeoutMs,
        @Value("${bitdance.sms.aliyun.read-timeout-ms:10000}") int readTimeoutMs
    ) {
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.templateParamName = templateParamName;
        this.templateMinParamName = templateMinParamName;
        this.ttlMinutes = ttlMinutes;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    @Override
    public void sendCode(String phone, String code) {
        if (!"aliyun".equalsIgnoreCase(provider)) {
            throw new BizException("SMS_PROVIDER_NOT_SUPPORTED", "暂不支持的短信服务商");
        }

        validateConfig();

        try {
            Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint)
                .setConnectTimeout(connectTimeoutMs)
                .setReadTimeout(readTimeoutMs);
            Client client = new Client(config);
            com.aliyun.dysmsapi20170525.models.SendSmsRequest request =
                new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParams(code));

            SendSmsResponse response = client.sendSms(request);
            String responseCode = response.getBody() == null ? null : response.getBody().getCode();
            if (!"OK".equalsIgnoreCase(responseCode)) {
                String message = response.getBody() == null ? "服务商未返回原因" : response.getBody().getMessage();
                throw new BizException("SMS_SEND_FAILED", "短信发送失败：" + message);
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("SMS_SEND_FAILED", "短信发送失败：" + ex.getMessage());
        }
    }

    private void validateConfig() {
        List<String> missing = new ArrayList<>();
        require(accessKeyId, "ALIYUN_SMS_ACCESS_KEY_ID", missing);
        require(accessKeySecret, "ALIYUN_SMS_ACCESS_KEY_SECRET", missing);
        require(signName, "ALIYUN_SMS_SIGN_NAME", missing);
        require(templateCode, "ALIYUN_SMS_TEMPLATE_CODE", missing);
        require(templateParamName, "ALIYUN_SMS_TEMPLATE_PARAM_NAME", missing);
        if (!missing.isEmpty()) {
            throw new BizException("SMS_PROVIDER_NOT_CONFIGURED", "短信服务商配置不完整：" + String.join(", ", missing));
        }
    }

    private void require(String value, String name, List<String> missing) {
        if (!StringUtils.hasText(value)) {
            missing.add(name);
        }
    }

    private String templateParams(String code) throws JsonProcessingException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(templateParamName, code);
        if (StringUtils.hasText(templateMinParamName)) {
            params.put(templateMinParamName, String.valueOf(ttlMinutes));
        }
        return objectMapper.writeValueAsString(params);
    }
}
