package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.UserSummary;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.HexFormat;

@Service
public class AuthService {

    private final SmsCodeService smsCodeService;
    private final AppUserRepository userRepo;
    private final UserRoleBindingRepository roleRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final WechatOAuthClient wechatOAuthClient;
    private final boolean wechatAutoCreateUser;

    public AuthService(
        SmsCodeService smsCodeService,
        AppUserRepository userRepo,
        UserRoleBindingRepository roleRepo,
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        WechatOAuthClient wechatOAuthClient,
        @Value("${bitdance.wechat.auto-create-user:false}") boolean wechatAutoCreateUser
    ) {
        this.smsCodeService = smsCodeService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.wechatOAuthClient = wechatOAuthClient;
        this.wechatAutoCreateUser = wechatAutoCreateUser;
    }

    public void sendSmsCode(String phone) {
        smsCodeService.send(phone);
    }

    @Transactional
    public LoginResponse loginWithSms(String phone, String code) {
        smsCodeService.verify(phone, code);
        AppUser user = userRepo.findByPhone(phone).orElseGet(() -> registerUser(phone, null));
        return issueFor(user);
    }

    @Transactional
    public LoginResponse loginWithPassword(String phone, String password) {
        AppUser user = userRepo.findByPhone(phone)
            .orElseThrow(() -> new BizException("AUTH_INVALID", "手机号或密码错误"));
        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BizException("AUTH_INVALID", "手机号或密码错误");
        }
        return issueFor(user);
    }

    @Transactional
    public LoginResponse loginWithWechat(String code) {
        if (!code.startsWith("dev_mock_")) {
            WechatOAuthClient.WechatIdentity identity = wechatOAuthClient.exchangeCode(code);
            AppUser user = findWechatUser(identity)
                .orElseGet(() -> createWechatUser(identity));
            return issueFor(user);
        }
        String openId = "wx_" + code;
        AppUser user = userRepo.findByOpenId(openId)
            .orElseGet(() -> bindMockWechatAccount(openId));
        return issueFor(user);
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

    private java.util.Optional<AppUser> findWechatUser(WechatOAuthClient.WechatIdentity identity) {
        if (StringUtils.hasText(identity.unionId())) {
            java.util.Optional<AppUser> byUnionId = userRepo.findByUnionId(identity.unionId());
            if (byUnionId.isPresent()) {
                return byUnionId;
            }
        }
        return userRepo.findByOpenId(identity.openId());
    }

    private AppUser createWechatUser(WechatOAuthClient.WechatIdentity identity) {
        if (!wechatAutoCreateUser) {
            throw new BizException("WECHAT_BIND_PHONE_REQUIRED", "微信授权成功，请先使用手机号登录后绑定微信");
        }
        AppUser user = registerUser("wx" + shortHash(identity.openId()), null);
        user.setOpenId(identity.openId());
        user.setUnionId(identity.unionId());
        return userRepo.save(user);
    }

    private String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes).substring(0, 18);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private AppUser bindMockWechatAccount(String openId) {
        AppUser user = userRepo.findByPhone("13900000005")
            .orElseThrow(() -> new BizException("WECHAT_DEV_USER_MISSING", "开发微信模拟账号不存在"));
        user.setOpenId(openId);
        user.setUnionId("union_" + openId);
        return userRepo.save(user);
    }

    private LoginResponse issueFor(AppUser user) {
        List<String> roles = roleRepo.findByUserIdAndStatus(user.getId(), "ACTIVE")
            .stream().map(UserRoleBinding::getRole).toList();
        String token = jwtService.issueAccessToken(user.getId(), roles);
        return new LoginResponse(token, new UserSummary(
            user.getId(), user.getPhone(), maskNick(user.getPhone()), null, roles
        ));
    }

    private String maskNick(String phone) {
        return "舞者" + phone.substring(phone.length() - 4);
    }
}
