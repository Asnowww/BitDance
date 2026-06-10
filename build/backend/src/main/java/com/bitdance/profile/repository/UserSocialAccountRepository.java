package com.bitdance.profile.repository;

import com.bitdance.profile.domain.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    List<UserSocialAccount> findByUserIdOrderByIdAsc(Long userId);
    List<UserSocialAccount> findByUserIdAndIsPublicTrueOrderByIdAsc(Long userId);
    Optional<UserSocialAccount> findByIdAndUserId(Long id, Long userId);
}
