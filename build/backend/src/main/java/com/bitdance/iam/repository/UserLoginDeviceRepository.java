package com.bitdance.iam.repository;

import com.bitdance.iam.domain.UserLoginDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLoginDeviceRepository extends JpaRepository<UserLoginDevice, Long> {
    List<UserLoginDevice> findByUserIdOrderByLastLoginAtDesc(Long userId);
    Optional<UserLoginDevice> findByIdAndUserId(Long id, Long userId);
}
