package com.bitdance.profile.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.profile.domain.DanceStyle;
import com.bitdance.profile.domain.PrivacySetting;
import com.bitdance.profile.domain.UserDancePreference;
import com.bitdance.profile.domain.UserProfile;
import com.bitdance.profile.dto.PrivacyDto;
import com.bitdance.profile.dto.ProfileResponse;
import com.bitdance.profile.dto.StylePreferenceDto;
import com.bitdance.profile.dto.UpdateProfileRequest;
import com.bitdance.profile.repository.DanceStyleRepository;
import com.bitdance.profile.repository.PrivacySettingRepository;
import com.bitdance.profile.repository.UserDancePreferenceRepository;
import com.bitdance.profile.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProfileService {

    private static final Set<String> GENDERS = Set.of("male", "female", "unknown");
    private static final Set<String> VISIBILITY = Set.of("public", "followers", "private");

    private final UserProfileRepository profileRepo;
    private final PrivacySettingRepository privacyRepo;
    private final UserDancePreferenceRepository stylePrefRepo;
    private final DanceStyleRepository danceStyleRepo;
    private final AppUserRepository userRepo;

    public ProfileService(
        UserProfileRepository profileRepo,
        PrivacySettingRepository privacyRepo,
        UserDancePreferenceRepository stylePrefRepo,
        DanceStyleRepository danceStyleRepo,
        AppUserRepository userRepo
    ) {
        this.profileRepo = profileRepo;
        this.privacyRepo = privacyRepo;
        this.stylePrefRepo = stylePrefRepo;
        this.danceStyleRepo = danceStyleRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(Long userId) {
        UserProfile p = profileRepo.findById(userId).orElseGet(() -> defaultProfile(userId));
        PrivacySetting pr = privacyRepo.findById(userId).orElseGet(() -> defaultPrivacy(userId));
        List<UserDancePreference> prefs = stylePrefRepo.findByUserId(userId);
        Map<Long, String> styleNames = loadStyleNames(prefs);

        List<StylePreferenceDto> styleDtos = prefs.stream()
            .map(it -> new StylePreferenceDto(
                it.getDanceStyleId(),
                styleNames.getOrDefault(it.getDanceStyleId(), ""),
                it.getSkillLevel(),
                Boolean.TRUE.equals(it.getIsPrimary())
            ))
            .toList();

        return new ProfileResponse(
            userId,
            p.getNickname(),
            p.getAvatarAssetId(),
            p.getGender(),
            p.getBirthday(),
            p.getBio(),
            p.getCityId(),
            p.getCurrentLevel(),
            p.getLearningGoal(),
            styleDtos,
            new PrivacyDto(
                pr.getProfileVisibility(),
                pr.getGrowthVisibility(),
                pr.getPracticeVisibility(),
                pr.getContentVisibility()
            )
        );
    }

    @Transactional
    public ProfileResponse update(Long userId, UpdateProfileRequest req) {
        if (!userRepo.existsById(userId)) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        UserProfile p = profileRepo.findById(userId).orElseGet(() -> defaultProfile(userId));

        if (req.nickname() != null && !req.nickname().isBlank()) {
            p.setNickname(req.nickname());
        }
        if (req.avatarAssetId() != null) p.setAvatarAssetId(req.avatarAssetId());
        if (req.gender() != null) {
            if (!GENDERS.contains(req.gender())) {
                throw new BizException("INVALID_ARGUMENT", "gender 非法");
            }
            p.setGender(req.gender());
        }
        if (req.birthday() != null) p.setBirthday(req.birthday());
        if (req.bio() != null) p.setBio(req.bio());
        if (req.cityId() != null) p.setCityId(req.cityId());
        if (req.currentLevel() != null) p.setCurrentLevel(req.currentLevel());
        if (req.learningGoal() != null) p.setLearningGoal(req.learningGoal());
        profileRepo.save(p);

        if (req.privacy() != null) {
            PrivacySetting pr = privacyRepo.findById(userId).orElseGet(() -> defaultPrivacy(userId));
            applyPrivacy(pr, req.privacy());
            privacyRepo.save(pr);
        }

        if (req.styles() != null) {
            stylePrefRepo.deleteByUserId(userId);
            stylePrefRepo.flush();
            for (StylePreferenceDto s : req.styles()) {
                if (s.danceStyleId() == null) continue;
                UserDancePreference pref = new UserDancePreference();
                pref.setUserId(userId);
                pref.setDanceStyleId(s.danceStyleId());
                pref.setSkillLevel(s.skillLevel());
                pref.setIsPrimary(Boolean.TRUE.equals(s.isPrimary()));
                stylePrefRepo.save(pref);
            }
        }

        return get(userId);
    }

    private void applyPrivacy(PrivacySetting pr, PrivacyDto dto) {
        if (dto.profileVisibility() != null) {
            assertVisibility(dto.profileVisibility());
            pr.setProfileVisibility(dto.profileVisibility());
        }
        if (dto.growthVisibility() != null) {
            assertVisibility(dto.growthVisibility());
            pr.setGrowthVisibility(dto.growthVisibility());
        }
        if (dto.practiceVisibility() != null) {
            assertVisibility(dto.practiceVisibility());
            pr.setPracticeVisibility(dto.practiceVisibility());
        }
        if (dto.contentVisibility() != null) {
            assertVisibility(dto.contentVisibility());
            pr.setContentVisibility(dto.contentVisibility());
        }
    }

    private void assertVisibility(String v) {
        if (!VISIBILITY.contains(v)) {
            throw new BizException("INVALID_ARGUMENT", "visibility 必须是 public/followers/private");
        }
    }

    private UserProfile defaultProfile(Long userId) {
        UserProfile p = new UserProfile();
        p.setUserId(userId);
        p.setNickname("舞者" + userId);
        p.setGender("unknown");
        return p;
    }

    private PrivacySetting defaultPrivacy(Long userId) {
        PrivacySetting pr = new PrivacySetting();
        pr.setUserId(userId);
        return pr;
    }

    private Map<Long, String> loadStyleNames(List<UserDancePreference> prefs) {
        if (prefs.isEmpty()) return Map.of();
        Map<Long, String> out = new HashMap<>();
        for (DanceStyle s : danceStyleRepo.findAllById(prefs.stream().map(UserDancePreference::getDanceStyleId).toList())) {
            out.put(s.getId(), s.getNameZh());
        }
        return out;
    }
}
