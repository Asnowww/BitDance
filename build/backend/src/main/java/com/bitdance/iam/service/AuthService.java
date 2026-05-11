package com.bitdance.iam.service;

import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.dto.LoginResponse;
import com.bitdance.iam.dto.UserSummary;
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final SmsCodeService smsCodeService;
    private final AppUserRepository userRepo;
    private final UserRoleBindingRepository roleRepo;
    private final JwtService jwtService;

    public AuthService(
        SmsCodeService smsCodeService,
        AppUserRepository userRepo,
        UserRoleBindingRepository roleRepo,
        JwtService jwtService
    ) {
        this.smsCodeService = smsCodeService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
    }

    public void sendSmsCode(String phone) {
        smsCodeService.send(phone);
    }

    @Transactional
    public LoginResponse loginWithSms(String phone, String code) {
        smsCodeService.verify(phone, code);
        AppUser user = userRepo.findByPhone(phone).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setPhone(phone);
            u.setStatus("ACTIVE");
            AppUser saved = userRepo.save(u);
            UserRoleBinding bind = new UserRoleBinding();
            bind.setUserId(saved.getId());
            bind.setRole("USER");
            bind.setStatus("ACTIVE");
            roleRepo.save(bind);
            return saved;
        });
        List<String> roles = roleRepo.findByUserIdAndStatus(user.getId(), "ACTIVE")
            .stream().map(UserRoleBinding::getRole).toList();
        String token = jwtService.issueAccessToken(user.getId(), roles);
        return new LoginResponse(token, new UserSummary(
            user.getId(), user.getPhone(), maskNick(phone), null, roles
        ));
    }

    private String maskNick(String phone) {
        return "舞者" + phone.substring(phone.length() - 4);
    }
}
