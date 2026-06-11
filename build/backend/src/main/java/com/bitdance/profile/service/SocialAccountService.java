package com.bitdance.profile.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.profile.domain.UserSocialAccount;
import com.bitdance.profile.dto.SocialAccountDto;
import com.bitdance.profile.dto.UpdateSocialAccountRequest;
import com.bitdance.profile.repository.UserSocialAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SocialAccountService {

    private final UserSocialAccountRepository repo;

    public SocialAccountService(UserSocialAccountRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<SocialAccountDto> mine(Long userId) {
        return repo.findByUserIdOrderByIdAsc(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SocialAccountDto> publicAccounts(Long userId) {
        return repo.findByUserIdAndIsPublicTrueOrderByIdAsc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public SocialAccountDto updateMine(Long userId, Long id, UpdateSocialAccountRequest req) {
        UserSocialAccount account = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new BizException("SOCIAL_ACCOUNT_NOT_FOUND", "社交账号不存在"));
        account.setIsPublic(req.isPublic());
        return toDto(repo.save(account));
    }

    private SocialAccountDto toDto(UserSocialAccount account) {
        return new SocialAccountDto(
            account.getId(),
            account.getUserId(),
            account.getPlatform(),
            account.getAccountName(),
            account.getProfileUrl(),
            account.getIsPublic()
        );
    }
}
