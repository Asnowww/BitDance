package com.bitdance.iam.controller;

import com.bitdance.common.exception.BizException;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.dto.UserSummary;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import com.bitdance.iam.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/h5/me")
public class MeController {

    private final AppUserRepository userRepo;
    private final UserRoleBindingRepository roleRepo;

    public MeController(AppUserRepository userRepo, UserRoleBindingRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @GetMapping
    public ApiResponse<UserSummary> me() {
        Long uid = CurrentUser.getId();
        AppUser user = userRepo.findById(uid)
            .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        var roles = roleRepo.findByUserIdAndStatus(uid, "ACTIVE")
            .stream().map(UserRoleBinding::getRole).toList();
        return ApiResponse.ok(new UserSummary(
            user.getId(),
            user.getPhone(),
            "舞者" + user.getPhone().substring(user.getPhone().length() - 4),
            null,
            roles
        ));
    }
}
