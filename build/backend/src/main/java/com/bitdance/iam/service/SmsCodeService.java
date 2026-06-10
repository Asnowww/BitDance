package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {

    private static final String CODE_KEY = "auth:sms:%s";
    private static final String COOLDOWN_KEY = "auth:sms:cd:%s";

    private final StringRedisTemplate redis;
    private final SmsSender smsSender;
    private final AliyunPnvsSmsVerifier pnvsSmsVerifier;
    private final boolean mockMode;
    private final String provider;
    private final String fixedCode;
    private final long cooldownSeconds;
    private final long ttlMinutes;
    private final String storage;
    private final Map<String, StoredCode> memoryCodes = new ConcurrentHashMap<>();
    private final Map<String, Instant> memoryCooldowns = new ConcurrentHashMap<>();

    public SmsCodeService(
        StringRedisTemplate redis,
        SmsSender smsSender,
        AliyunPnvsSmsVerifier pnvsSmsVerifier,
        @Value("${bitdance.sms.mock:true}") boolean mockMode,
        @Value("${bitdance.sms.provider:aliyun}") String provider,
        @Value("${bitdance.sms.fixed-code:123456}") String fixedCode,
        @Value("${bitdance.sms.cooldown-seconds:60}") long cooldownSeconds,
        @Value("${bitdance.sms.ttl-minutes:5}") long ttlMinutes,
        @Value("${bitdance.sms.storage:memory}") String storage
    ) {
        this.redis = redis;
        this.smsSender = smsSender;
        this.pnvsSmsVerifier = pnvsSmsVerifier;
        this.mockMode = mockMode;
        this.provider = provider;
        this.fixedCode = fixedCode;
        this.cooldownSeconds = cooldownSeconds;
        this.ttlMinutes = ttlMinutes;
        this.storage = storage;
    }

    public void send(String phone) {
        Instant now = Instant.now();
        if (mockMode) {
            reserveCooldown(phone, now);
            storeCode(phone, fixedCode, now);
            return;
        }

        reserveCooldown(phone, now);
        if (usePnvsProvider()) {
            try {
                pnvsSmsVerifier.sendCode(phone);
                return;
            } catch (RuntimeException ex) {
                clearCooldown(phone);
                throw ex;
            }
        }

        String code = generateCode();
        storeCode(phone, code, now);
        try {
            smsSender.sendCode(phone, code);
        } catch (RuntimeException ex) {
            clearCode(phone);
            clearCooldown(phone);
            throw ex;
        }
    }

    public void verify(String phone, String code) {
        if (!mockMode && usePnvsProvider()) {
            pnvsSmsVerifier.verifyCode(phone, code);
            return;
        }

        String stored = loadCode(phone);
        if (stored == null) {
            throw new BizException("SMS_EXPIRED", "验证码已过期");
        }
        if (!stored.equals(code)) {
            throw new BizException("SMS_INVALID", "验证码错误");
        }
        clearCode(phone);
    }

    private void reserveCooldown(String phone, Instant now) {
        if (useRedisStorage()) {
            Boolean ok = redis.opsForValue().setIfAbsent(
                COOLDOWN_KEY.formatted(phone),
                "1",
                Duration.ofSeconds(cooldownSeconds)
            );
            if (Boolean.FALSE.equals(ok)) {
                throw new BizException("SMS_COOLDOWN", "请稍后再试");
            }
            return;
        }

        synchronized (memoryCooldowns) {
            Instant cooldownUntil = memoryCooldowns.get(phone);
            if (cooldownUntil != null && cooldownUntil.isAfter(now)) {
                throw new BizException("SMS_COOLDOWN", "请稍后再试");
            }
            memoryCooldowns.put(phone, now.plusSeconds(cooldownSeconds));
        }
    }

    private void storeCode(String phone, String code, Instant now) {
        if (useRedisStorage()) {
            redis.opsForValue().set(CODE_KEY.formatted(phone), code, Duration.ofMinutes(ttlMinutes));
            return;
        }
        memoryCodes.put(phone, new StoredCode(code, now.plus(Duration.ofMinutes(ttlMinutes))));
    }

    private String loadCode(String phone) {
        if (useRedisStorage()) {
            return redis.opsForValue().get(CODE_KEY.formatted(phone));
        }

        StoredCode stored = memoryCodes.get(phone);
        if (stored == null) {
            return null;
        }
        if (stored.expiresAt().isBefore(Instant.now())) {
            memoryCodes.remove(phone);
            return null;
        }
        return stored.code();
    }

    private void clearCode(String phone) {
        if (useRedisStorage()) {
            redis.delete(CODE_KEY.formatted(phone));
            return;
        }
        memoryCodes.remove(phone);
    }

    private void clearCooldown(String phone) {
        if (useRedisStorage()) {
            redis.delete(COOLDOWN_KEY.formatted(phone));
            return;
        }
        memoryCooldowns.remove(phone);
    }

    private boolean useRedisStorage() {
        return "redis".equalsIgnoreCase(storage);
    }

    private boolean usePnvsProvider() {
        String normalized = provider == null ? "" : provider.toLowerCase(Locale.ROOT);
        return normalized.equals("aliyun-pnvs")
            || normalized.equals("aliyun-dypns")
            || normalized.equals("pnvs")
            || normalized.equals("dypns");
    }

    private String generateCode() {
        SecureRandom rnd = new SecureRandom();
        return String.format("%06d", rnd.nextInt(1_000_000));
    }

    private record StoredCode(String code, Instant expiresAt) {
    }
}
