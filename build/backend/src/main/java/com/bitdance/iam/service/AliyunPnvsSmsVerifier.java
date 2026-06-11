package com.bitdance.iam.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.bitdance.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AliyunPnvsSmsVerifier {

    private static final Logger log = LoggerFactory.getLogger(AliyunPnvsSmsVerifier.class);
    private static final ExecutorService PROVIDER_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "aliyun-pnvs-client");
        thread.setDaemon(true);
        return thread;
    });

    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final String templateParamName;
    private final String templateMinParamName;
    private final String codePlaceholder;
    private final String countryCode;
    private final String schemeName;
    private final long ttlMinutes;
    private final long cooldownSeconds;
    private final long codeLength;
    private final long codeType;
    private final long caseAuthPolicy;
    private final long duplicatePolicy;
    private final long autoRetry;
    private final boolean returnVerifyCode;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int callTimeoutMs;
    private final int maxAttempts;
    private final long retryBackoffMs;

    public AliyunPnvsSmsVerifier(
        ObjectMapper objectMapper,
        @Value("${bitdance.sms.aliyun-pnvs.endpoint:dypnsapi.aliyuncs.com}") String endpoint,
        @Value("${bitdance.sms.aliyun-pnvs.access-key-id:}") String accessKeyId,
        @Value("${bitdance.sms.aliyun-pnvs.access-key-secret:}") String accessKeySecret,
        @Value("${bitdance.sms.aliyun-pnvs.sign-name:}") String signName,
        @Value("${bitdance.sms.aliyun-pnvs.template-code:}") String templateCode,
        @Value("${bitdance.sms.aliyun-pnvs.template-param-name:code}") String templateParamName,
        @Value("${bitdance.sms.aliyun-pnvs.template-min-param-name:min}") String templateMinParamName,
        @Value("${bitdance.sms.aliyun-pnvs.code-placeholder:##code##}") String codePlaceholder,
        @Value("${bitdance.sms.aliyun-pnvs.country-code:86}") String countryCode,
        @Value("${bitdance.sms.aliyun-pnvs.scheme-name:}") String schemeName,
        @Value("${bitdance.sms.ttl-minutes:5}") long ttlMinutes,
        @Value("${bitdance.sms.cooldown-seconds:60}") long cooldownSeconds,
        @Value("${bitdance.sms.aliyun-pnvs.code-length:6}") long codeLength,
        @Value("${bitdance.sms.aliyun-pnvs.code-type:1}") long codeType,
        @Value("${bitdance.sms.aliyun-pnvs.case-auth-policy:1}") long caseAuthPolicy,
        @Value("${bitdance.sms.aliyun-pnvs.duplicate-policy:1}") long duplicatePolicy,
        @Value("${bitdance.sms.aliyun-pnvs.auto-retry:1}") long autoRetry,
        @Value("${bitdance.sms.aliyun-pnvs.return-verify-code:false}") boolean returnVerifyCode,
        @Value("${bitdance.sms.aliyun-pnvs.connect-timeout-ms:10000}") int connectTimeoutMs,
        @Value("${bitdance.sms.aliyun-pnvs.read-timeout-ms:10000}") int readTimeoutMs,
        @Value("${bitdance.sms.aliyun-pnvs.call-timeout-ms:15000}") int callTimeoutMs,
        @Value("${bitdance.sms.aliyun-pnvs.max-attempts:2}") int maxAttempts,
        @Value("${bitdance.sms.aliyun-pnvs.retry-backoff-ms:300}") long retryBackoffMs
    ) {
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.templateParamName = templateParamName;
        this.templateMinParamName = templateMinParamName;
        this.codePlaceholder = codePlaceholder;
        this.countryCode = countryCode;
        this.schemeName = schemeName;
        this.ttlMinutes = ttlMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.codeLength = codeLength;
        this.codeType = codeType;
        this.caseAuthPolicy = caseAuthPolicy;
        this.duplicatePolicy = duplicatePolicy;
        this.autoRetry = autoRetry;
        this.returnVerifyCode = returnVerifyCode;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.callTimeoutMs = callTimeoutMs;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
    }

    public void sendCode(String phone) {
        validateConfig();
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParams())
                .setCountryCode(countryCode)
                .setInterval(cooldownSeconds)
                .setValidTime(ttlMinutes * 60)
                .setCodeLength(codeLength)
                .setCodeType(codeType)
                .setDuplicatePolicy(duplicatePolicy)
                .setAutoRetry(autoRetry)
                .setReturnVerifyCode(returnVerifyCode);
            if (StringUtils.hasText(schemeName)) {
                request.setSchemeName(schemeName);
            }

            SendSmsVerifyCodeResponse response = callProvider(
                "send",
                () -> client().sendSmsVerifyCodeWithOptions(request, runtimeOptions())
            );
            SendSmsVerifyCodeResponseBody body = response.getBody();
            log.info(
                "aliyun pnvs send result phone={} code={} success={} requestId={} message={}",
                maskPhone(phone),
                body == null ? null : body.getCode(),
                body == null ? null : body.getSuccess(),
                body == null ? null : body.getRequestId(),
                body == null ? null : body.getMessage()
            );
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(body.getCode())) {
                String message = body == null ? "provider returned empty response" : body.getMessage();
                throw new BizException("SMS_SEND_FAILED", "短信发送失败：" + message);
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("SMS_SEND_FAILED", "短信发送失败：" + ex.getMessage());
        }
    }

    public void verifyCode(String phone, String code) {
        validateConfig();
        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setVerifyCode(code)
                .setCountryCode(countryCode)
                .setCaseAuthPolicy(caseAuthPolicy);
            if (StringUtils.hasText(schemeName)) {
                request.setSchemeName(schemeName);
            }

            CheckSmsVerifyCodeResponse response = callProvider(
                "verify",
                () -> client().checkSmsVerifyCodeWithOptions(request, runtimeOptions())
            );
            CheckSmsVerifyCodeResponseBody body = response.getBody();
            String result = body == null || body.getModel() == null ? null : body.getModel().getVerifyResult();
            log.info(
                "aliyun pnvs verify result phone={} code={} verifyResult={} message={}",
                maskPhone(phone),
                body == null ? null : body.getCode(),
                result,
                body == null ? null : body.getMessage()
            );
            if ("PASS".equalsIgnoreCase(result)) {
                return;
            }
            if (body != null && "OK".equalsIgnoreCase(body.getCode())) {
                throw new BizException("SMS_INVALID", "验证码错误");
            }
            String message = body == null ? "provider returned empty response" : body.getMessage();
            throw new BizException("SMS_VERIFY_FAILED", "验证码校验失败：" + message);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("SMS_VERIFY_FAILED", "验证码校验失败：" + ex.getMessage());
        }
    }

    private Client client() throws Exception {
        Config config = new Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
            .setEndpoint(endpoint)
            .setConnectTimeout(connectTimeoutMs)
            .setReadTimeout(readTimeoutMs);
        return new Client(config);
    }

    private RuntimeOptions runtimeOptions() {
        return new RuntimeOptions()
            .setConnectTimeout(connectTimeoutMs)
            .setReadTimeout(readTimeoutMs);
    }

    private void validateConfig() {
        List<String> missing = new ArrayList<>();
        require(accessKeyId, "ALIYUN_PNVS_ACCESS_KEY_ID or ALIYUN_SMS_ACCESS_KEY_ID", missing);
        require(accessKeySecret, "ALIYUN_PNVS_ACCESS_KEY_SECRET or ALIYUN_SMS_ACCESS_KEY_SECRET", missing);
        require(signName, "ALIYUN_PNVS_SIGN_NAME or ALIYUN_SMS_SIGN_NAME", missing);
        require(templateCode, "ALIYUN_PNVS_TEMPLATE_CODE or ALIYUN_SMS_TEMPLATE_CODE", missing);
        require(templateParamName, "ALIYUN_PNVS_TEMPLATE_PARAM_NAME", missing);
        require(codePlaceholder, "ALIYUN_PNVS_CODE_PLACEHOLDER", missing);
        if (!missing.isEmpty()) {
            throw new BizException("SMS_PROVIDER_NOT_CONFIGURED", "短信认证服务配置不完整：" + String.join(", ", missing));
        }
    }

    private void require(String value, String name, List<String> missing) {
        if (!StringUtils.hasText(value)) {
            missing.add(name);
        }
    }

    private String templateParams() throws JsonProcessingException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(templateParamName, codePlaceholder);
        if (StringUtils.hasText(templateMinParamName)) {
            params.put(templateMinParamName, String.valueOf(ttlMinutes));
        }
        return objectMapper.writeValueAsString(params);
    }

    private <T> T callProvider(String action, Callable<T> callable) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callProviderOnce(action, callable, attempt);
            } catch (BizException ex) {
                last = ex;
                if (!isRetryableProviderError(ex) || attempt >= maxAttempts) {
                    throw ex;
                }
                log.warn("aliyun pnvs {} attempt {}/{} failed: {}", action, attempt, maxAttempts, ex.getMessage());
                sleepBeforeRetry();
            }
        }
        throw last == null ? new BizException("SMS_PROVIDER_ERROR", "短信认证服务调用失败") : last;
    }

    private <T> T callProviderOnce(String action, Callable<T> callable, int attempt) {
        Future<T> future = PROVIDER_EXECUTOR.submit(callable);
        try {
            return future.get(callTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            log.warn("aliyun pnvs {} attempt {} timed out after {}ms", action, attempt, callTimeoutMs);
            throw new BizException("SMS_PROVIDER_TIMEOUT", "短信认证服务响应超时，请稍后再试");
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof BizException bizException) {
                throw bizException;
            }
            String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
            throw new BizException("SMS_PROVIDER_ERROR", "短信认证服务调用失败：" + message);
        }
    }

    private boolean isRetryableProviderError(BizException ex) {
        String code = ex.getCode();
        return "SMS_PROVIDER_TIMEOUT".equals(code) || "SMS_PROVIDER_ERROR".equals(code);
    }

    private void sleepBeforeRetry() {
        if (retryBackoffMs <= 0) {
            return;
        }
        try {
            Thread.sleep(retryBackoffMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
