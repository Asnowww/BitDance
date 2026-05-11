package com.bitdance.iam.repository;

import com.bitdance.iam.domain.UserRoleBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleBindingRepository extends JpaRepository<UserRoleBinding, Long> {
    List<UserRoleBinding> findByUserIdAndStatus(Long userId, String status);
}
