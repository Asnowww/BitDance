package com.bitdance.iam.service;

import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.RoleCode;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 开发环境测试账号播种器。
 * 仅在非 prod profile 且 bitdance.dev.seed-account=true 时生效；生产环境不会创建任何账号。
 * 账号通过正常的 BCrypt 密码校验登录，不是鉴权旁路。
 */
@Component
@Profile("!prod")
public class DevAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAccountSeeder.class);

    private final AuthService authService;
    private final UserRoleBindingRepository roleRepo;
    private final boolean enabled;
    private final String phone;
    private final String password;

    public DevAccountSeeder(
        AuthService authService,
        UserRoleBindingRepository roleRepo,
        @Value("${bitdance.dev.seed-account:false}") boolean enabled,
        @Value("${bitdance.dev.seed-account-phone:18511695975}") String phone,
        @Value("${bitdance.dev.seed-account-password:123456}") String password
    ) {
        this.authService = authService;
        this.roleRepo = roleRepo;
        this.enabled = enabled;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        try {
            AppUser user = authService.ensureAccount(phone, password);
            ensureRole(user.getId(), RoleCode.COACH);
            ensureRole(user.getId(), RoleCode.STUDIO_ADMIN);
            ensureRole(user.getId(), RoleCode.PLATFORM_ADMIN);
            log.info("[dev] test account ensured phone={} with all roles (password login)", phone);
        } catch (Exception ex) {
            log.warn("[dev] test account seed failed: {}", ex.getMessage());
        }
    }

    private void ensureRole(Long userId, RoleCode role) {
        roleRepo.findFirstByUserIdAndRoleOrderByIdDesc(userId, role).ifPresentOrElse(
            binding -> {
                if (!"ACTIVE".equals(binding.getStatus())) {
                    binding.setStatus("ACTIVE");
                    roleRepo.save(binding);
                }
            },
            () -> {
                UserRoleBinding binding = new UserRoleBinding();
                binding.setUserId(userId);
                binding.setRole(role);
                binding.setStatus("ACTIVE");
                roleRepo.save(binding);
            }
        );
    }
}
