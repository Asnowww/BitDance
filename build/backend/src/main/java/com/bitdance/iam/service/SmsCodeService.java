package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {

    private static final String CODE_KEY = "auth:sms:%s";
    private static final String COOLDOWN_KEY = "auth:sms:cd:%s";

    private final StringRedisTemplate redis;
    private final boolean mockMode;
    private final String fixedCode;
    private final long cooldownSeconds;
    private final Map<String, MockCode> mockCodes = new ConcurrentHashMap<>();
    private final Map<String, Instant> mockCooldownUntil = new ConcurrentHashMap<>();

    public SmsCodeService(
        StringRedisTemplate redis,
        @Value("${bitdance.sms.mock:true}") boolean mockMode,
        @Value("${bitdance.sms.fixed-code:123456}") String fixedCode,
        @Value("${bitdance.sms.cooldown-seconds:60}") long cooldownSeconds
    ) {
        this.redis = redis;
        this.mockMode = mockMode;
        this.fixedCode = fixedCode;
        this.cooldownSeconds = cooldownSeconds;
    }

    public void send(String phone) {
        if (mockMode) {
            sendMock(phone);
            return;
        }
        String cdKey = COOLDOWN_KEY.formatted(phone);
        Boolean ok = redis.opsForValue().setIfAbsent(cdKey, "1", Duration.ofSeconds(cooldownSeconds));
        if (Boolean.FALSE.equals(ok)) {
            throw new BizException("SMS_COOLDOWN", "请稍后再试");
        }
        String code = mockMode ? fixedCode : generateCode();
        redis.opsForValue().set(CODE_KEY.formatted(phone), code, Duration.ofMinutes(5));
        // 生产实现：调用 SMS 网关；mock 模式仅写 Redis
    }

    public void verify(String phone, String code) {
        if (mockMode) {
            verifyMock(phone, code);
            return;
        }
        String stored = redis.opsForValue().get(CODE_KEY.formatted(phone));
        if (stored == null) {
            throw new BizException("SMS_EXPIRED", "验证码已过期");
        }
        if (!stored.equals(code)) {
            throw new BizException("SMS_INVALID", "验证码错误");
        }
        redis.delete(CODE_KEY.formatted(phone));
    }

    private void sendMock(String phone) {
        Instant now = Instant.now();
        Instant cooldownUntil = mockCooldownUntil.get(phone);
        if (cooldownUntil != null && cooldownUntil.isAfter(now)) {
            throw new BizException("SMS_COOLDOWN", "请稍后再试");
        }
        mockCooldownUntil.put(phone, now.plusSeconds(cooldownSeconds));
        mockCodes.put(phone, new MockCode(fixedCode, now.plus(Duration.ofMinutes(5))));
    }

    private void verifyMock(String phone, String code) {
        MockCode stored = mockCodes.get(phone);
        if (stored == null || stored.expiresAt().isBefore(Instant.now())) {
            mockCodes.remove(phone);
            throw new BizException("SMS_EXPIRED", "验证码已过期");
        }
        if (!stored.code().equals(code)) {
            throw new BizException("SMS_INVALID", "验证码错误");
        }
        mockCodes.remove(phone);
    }

    private String generateCode() {
        SecureRandom rnd = new SecureRandom();
        return String.format("%06d", rnd.nextInt(1_000_000));
    }

    private record MockCode(String code, Instant expiresAt) {}
}
