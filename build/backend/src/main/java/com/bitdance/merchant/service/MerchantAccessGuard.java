package com.bitdance.merchant.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import com.bitdance.merchant.repository.StudioClaimRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 商家侧数据权限守卫：操作 studio 资源前必须确认操作者是该 studio 的已认领管理员
 * （或平台管理员，平台管理员可越权操作任意 studio）。
 *
 * 用法：service 层在每个 /merchant/** 接口入口注入并调用 requireStudioOwnership(userId, studioId)。
 */
@Component
public class MerchantAccessGuard {

    private final StudioClaimRepository claimRepo;
    private final UserRoleBindingRepository roleRepo;

    public MerchantAccessGuard(
        StudioClaimRepository claimRepo,
        UserRoleBindingRepository roleRepo
    ) {
        this.claimRepo = claimRepo;
        this.roleRepo = roleRepo;
    }

    public void requireStudioOwnership(Long userId, Long studioId) {
        if (isPlatformAdmin(userId)) return;
        Set<Long> ids = new HashSet<>(claimRepo.findApprovedStudioIds(userId));
        if (!ids.contains(studioId)) {
            throw new BizException("FORBIDDEN", "你不是该舞室的认领管理员");
        }
    }

    public Set<Long> approvedStudioIds(Long userId) {
        return new HashSet<>(claimRepo.findApprovedStudioIds(userId));
    }

    public boolean isPlatformAdmin(Long userId) {
        return roleRepo.findByUserIdAndStatus(userId, "ACTIVE").stream()
            .map(UserRoleBinding::getRole)
            .anyMatch("PLATFORM_ADMIN"::equals);
    }
}
