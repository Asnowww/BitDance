package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.UserSummary;
import com.bitdance.iam.dto.WechatLoginResponse;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Duration WECHAT_BIND_TOKEN_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsCodeService smsCodeService;
    private final AppUserRepository userRepo;
    private final UserRoleBindingRepository roleRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final WechatOAuthClient wechatOAuthClient;
    private final Map<String, PendingWechatBind> pendingWechatBinds = new ConcurrentHashMap<>();

    public AuthService(
        SmsCodeService smsCodeService,
        AppUserRepository userRepo,
        UserRoleBindingRepository roleRepo,
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        WechatOAuthClient wechatOAuthClient
    ) {
        this.smsCodeService = smsCodeService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.wechatOAuthClient = wechatOAuthClient;
    }

    public void sendSmsCode(String phone) {
        smsCodeService.send(phone);
    }

    @Transactional
    public LoginResponse loginWithSms(String phone, String code) {
        smsCodeService.verify(phone, code);
        AppUser user = userRepo.findByPhone(phone).orElseGet(() -> registerUser(phone, null));
        return issueFor(user, !StringUtils.hasText(user.getPasswordHash()));
    }

    @Transactional
    public LoginResponse loginWithPassword(String phone, String password) {
        AppUser user = userRepo.findByPhone(phone)
            .orElseThrow(() -> new BizException("AUTH_INVALID", "手机号或密码错误"));
        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BizException("AUTH_INVALID", "手机号或密码错误");
        }
        return issueFor(user, false);
    }

    @Transactional
    public WechatLoginResponse loginWithWechat(String code) {
        if (!code.startsWith("dev_mock_")) {
            WechatOAuthClient.WechatIdentity identity = wechatOAuthClient.exchangeCode(code);
            Optional<AppUser> user = findWechatUser(identity);
            if (user.isPresent() && hasRealPhone(user.get().getPhone())) {
                return WechatLoginResponse.loggedIn(issueFor(user.get(), !StringUtils.hasText(user.get().getPasswordHash())));
            }
            return WechatLoginResponse.phoneBindingRequired(
                createWechatBindToken(identity),
                WECHAT_BIND_TOKEN_TTL.toSeconds()
            );
        }
        String openId = "wx_" + code;
        AppUser user = userRepo.findByOpenId(openId)
            .orElseGet(() -> bindMockWechatAccount(openId));
        return WechatLoginResponse.loggedIn(issueFor(user, !StringUtils.hasText(user.getPasswordHash())));
    }

    @Transactional
    public LoginResponse bindWechatPhone(String bindToken, String phone, String code) {
        PendingWechatBind pending = consumeWechatBindToken(bindToken);
        smsCodeService.verify(phone, code);

        WechatOAuthClient.WechatIdentity identity = pending.identity();
        Optional<AppUser> existingWechatUser = findWechatUser(identity);
        Optional<AppUser> existingPhoneUser = userRepo.findByPhone(phone);

        AppUser user;
        if (existingWechatUser.isPresent()) {
            AppUser wechatUser = existingWechatUser.get();
            if (hasRealPhone(wechatUser.getPhone())) {
                return issueFor(wechatUser, !StringUtils.hasText(wechatUser.getPasswordHash()));
            }
            if (existingPhoneUser.isPresent() && !existingPhoneUser.get().getId().equals(wechatUser.getId())) {
                user = existingPhoneUser.get();
                ensureWechatIdentityAvailableFor(user, identity);
                wechatUser.setOpenId(null);
                wechatUser.setUnionId(null);
                userRepo.save(wechatUser);
            } else {
                user = wechatUser;
                user.setPhone(phone);
            }
        } else {
            user = existingPhoneUser.orElseGet(() -> registerUser(phone, null));
            ensureWechatIdentityAvailableFor(user, identity);
        }

        user.setOpenId(identity.openId());
        user.setUnionId(identity.unionId());
        user = userRepo.save(user);
        return issueFor(user, !StringUtils.hasText(user.getPasswordHash()));
    }

    @Transactional
    public LoginResponse setPassword(Long userId, String rawPassword) {
        AppUser user = userRepo.findById(userId)
            .orElseThrow(() -> new BizException("USER_NOT_FOUND", "账号不存在"));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepo.save(user);
        return issueFor(user, false);
    }

    /** 仅供开发环境播种测试账号使用：存在则补密码，不存在则建号并绑定 USER 角色。 */
    @Transactional
    public AppUser ensureAccount(String phone, String rawPassword) {
        return userRepo.findByPhone(phone).map(u -> {
            if (u.getPasswordHash() == null) {
                u.setPasswordHash(passwordEncoder.encode(rawPassword));
                userRepo.save(u);
            }
            return u;
        }).orElseGet(() -> registerUser(phone, rawPassword));
    }

    private AppUser registerUser(String phone, String rawPassword) {
        AppUser u = new AppUser();
        u.setPhone(phone);
        u.setStatus("ACTIVE");
        if (rawPassword != null) {
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        AppUser saved = userRepo.save(u);
        UserRoleBinding bind = new UserRoleBinding();
        bind.setUserId(saved.getId());
        bind.setRole("USER");
        bind.setStatus("ACTIVE");
        roleRepo.save(bind);
        return saved;
    }

    private Optional<AppUser> findWechatUser(WechatOAuthClient.WechatIdentity identity) {
        if (StringUtils.hasText(identity.unionId())) {
            Optional<AppUser> byUnionId = userRepo.findByUnionId(identity.unionId());
            if (byUnionId.isPresent()) {
                return byUnionId;
            }
        }
        return userRepo.findByOpenId(identity.openId());
    }

    private boolean hasRealPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private String createWechatBindToken(WechatOAuthClient.WechatIdentity identity) {
        cleanupExpiredWechatBindTokens();
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        pendingWechatBinds.put(token, new PendingWechatBind(identity, Instant.now().plus(WECHAT_BIND_TOKEN_TTL)));
        return token;
    }

    private PendingWechatBind consumeWechatBindToken(String token) {
        PendingWechatBind pending = pendingWechatBinds.remove(token);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new BizException("WECHAT_BIND_TOKEN_EXPIRED", "微信绑定状态已过期，请重新授权");
        }
        return pending;
    }

    private void cleanupExpiredWechatBindTokens() {
        Instant now = Instant.now();
        pendingWechatBinds.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private void ensureWechatIdentityAvailableFor(AppUser target, WechatOAuthClient.WechatIdentity identity) {
        if (StringUtils.hasText(target.getOpenId()) && !target.getOpenId().equals(identity.openId())) {
            throw new BizException("PHONE_ALREADY_BOUND_TO_WECHAT", "该手机号已绑定其他微信");
        }
        if (StringUtils.hasText(target.getUnionId())
            && StringUtils.hasText(identity.unionId())
            && !target.getUnionId().equals(identity.unionId())) {
            throw new BizException("PHONE_ALREADY_BOUND_TO_WECHAT", "该手机号已绑定其他微信");
        }
        userRepo.findByOpenId(identity.openId())
            .filter(user -> !user.getId().equals(target.getId()))
            .ifPresent(user -> {
                throw new BizException("WECHAT_ALREADY_BOUND", "该微信已绑定其他账号");
            });
        if (StringUtils.hasText(identity.unionId())) {
            userRepo.findByUnionId(identity.unionId())
                .filter(user -> !user.getId().equals(target.getId()))
                .ifPresent(user -> {
                    throw new BizException("WECHAT_ALREADY_BOUND", "该微信已绑定其他账号");
                });
        }
    }

    private AppUser bindMockWechatAccount(String openId) {
        AppUser user = userRepo.findByPhone("13900000005")
            .orElseThrow(() -> new BizException("WECHAT_DEV_USER_MISSING", "开发微信模拟账号不存在"));
        user.setOpenId(openId);
        user.setUnionId("union_" + openId);
        return userRepo.save(user);
    }

    private LoginResponse issueFor(AppUser user, boolean passwordRequired) {
        List<String> roles = roleRepo.findByUserIdAndStatus(user.getId(), "ACTIVE")
            .stream().map(UserRoleBinding::getRole).toList();
        String token = jwtService.issueAccessToken(user.getId(), roles, passwordRequired);
        return new LoginResponse(token, new UserSummary(
            user.getId(), user.getPhone(), maskNick(user.getPhone()), null, roles
        ), passwordRequired);
    }

    private String maskNick(String phone) {
        return "舞者" + phone.substring(phone.length() - 4);
    }

    private record PendingWechatBind(WechatOAuthClient.WechatIdentity identity, Instant expiresAt) {
    }
}
