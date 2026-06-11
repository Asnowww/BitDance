package com.bitdance.iam.repository;

import com.bitdance.iam.domain.RoleCode;
import com.bitdance.iam.domain.UserRoleBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleBindingRepository extends JpaRepository<UserRoleBinding, Long> {
    List<UserRoleBinding> findByUserIdAndStatus(Long userId, String status);
    List<UserRoleBinding> findByUserIdOrderByIdDesc(Long userId);
    Optional<UserRoleBinding> findFirstByUserIdAndRoleOrderByIdDesc(Long userId, RoleCode role);
}
