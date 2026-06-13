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
    public SocialAccountDto createMine(Long userId, UpdateSocialAccountRequest req) {
        UserSocialAccount account = new UserSocialAccount();
        account.setUserId(userId);
        account.setPlatform(requiredText(req.platform(), "SOCIAL_PLATFORM_REQUIRED", "请填写平台名称"));
        account.setAccountName(requiredText(req.accountName(), "SOCIAL_ACCOUNT_NAME_REQUIRED", "请填写账号名称"));
        account.setProfileUrl(normalizeOptional(req.profileUrl()));
        account.setIsPublic(req.isPublic() == null || req.isPublic());
        return toDto(repo.save(account));
    }

    @Transactional
    public SocialAccountDto updateMine(Long userId, Long id, UpdateSocialAccountRequest req) {
        UserSocialAccount account = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new BizException("SOCIAL_ACCOUNT_NOT_FOUND", "社交账号不存在"));
        if (req.platform() != null) {
            account.setPlatform(requiredText(req.platform(), "SOCIAL_PLATFORM_REQUIRED", "请填写平台名称"));
        }
        if (req.accountName() != null) {
            account.setAccountName(requiredText(req.accountName(), "SOCIAL_ACCOUNT_NAME_REQUIRED", "请填写账号名称"));
        }
        if (req.profileUrl() != null) {
            account.setProfileUrl(normalizeOptional(req.profileUrl()));
        }
        if (req.isPublic() != null) {
            account.setIsPublic(req.isPublic());
        }
        return toDto(repo.save(account));
    }

    @Transactional
    public void deleteMine(Long userId, Long id) {
        UserSocialAccount account = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new BizException("SOCIAL_ACCOUNT_NOT_FOUND", "社交账号不存在"));
        repo.delete(account);
    }

    private String requiredText(String value, String code, String message) {
        String text = normalizeOptional(value);
        if (text == null) {
            throw new BizException(code, message);
        }
        return text;
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String text = value.trim();
        return text.isEmpty() ? null : text;
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
