package com.bitdance.iam.repository;

import com.bitdance.iam.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPhone(String phone);
    Optional<AppUser> findByOpenId(String openId);
}
