package com.bitdance.admin.service;

import com.bitdance.admin.dto.UserRoleBindingDto;
import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.RoleCode;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserRoleService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";

    private final AppUserRepository userRepo;
    private final UserRoleBindingRepository roleRepo;

    public AdminUserRoleService(AppUserRepository userRepo, UserRoleBindingRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional(readOnly = true)
    public List<UserRoleBindingDto> list(Long userId) {
        requireUser(userId);
        return roleRepo.findByUserIdOrderByIdDesc(userId).stream()
            .map(this::toDto)
            .toList();
    }

    @AuditAction(value = "user.role.grant", targetType = "user_role_binding")
    @Transactional
    public UserRoleBindingDto grant(Long userId, String roleValue) {
        requireUser(userId);
        RoleCode role = RoleCode.from(roleValue);
        UserRoleBinding binding = roleRepo.findFirstByUserIdAndRoleOrderByIdDesc(userId, role)
            .orElseGet(() -> {
                UserRoleBinding next = new UserRoleBinding();
                next.setUserId(userId);
                next.setRole(role);
                return next;
            });
        binding.setStatus(ACTIVE);
        return toDto(roleRepo.save(binding));
    }

    @AuditAction(value = "user.role.revoke", targetType = "user_role_binding")
    @Transactional
    public UserRoleBindingDto revoke(Long userId, String roleValue) {
        requireUser(userId);
        RoleCode role = RoleCode.from(roleValue);
        UserRoleBinding binding = roleRepo.findFirstByUserIdAndRoleOrderByIdDesc(userId, role)
            .orElseThrow(() -> new BizException("ROLE_BINDING_NOT_FOUND", "Role binding not found"));
        binding.setStatus(INACTIVE);
        return toDto(roleRepo.save(binding));
    }

    private AppUser requireUser(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new BizException("USER_NOT_FOUND", "User not found"));
    }

    private UserRoleBindingDto toDto(UserRoleBinding binding) {
        return new UserRoleBindingDto(
            binding.getId(),
            binding.getUserId(),
            binding.getRole(),
            binding.getStatus()
        );
    }
}
