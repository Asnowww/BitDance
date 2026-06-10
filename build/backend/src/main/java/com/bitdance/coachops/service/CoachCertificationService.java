package com.bitdance.coachops.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.repository.CoachByUserRepository;
import com.bitdance.coachops.domain.CoachCertificationApplication;
import com.bitdance.coachops.dto.CertificationDto;
import com.bitdance.coachops.dto.HandleCertificationRequest;
import com.bitdance.coachops.dto.SubmitCertificationRequest;
import com.bitdance.coachops.repository.CoachCertificationApplicationRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import com.bitdance.profile.domain.UserProfile;
import com.bitdance.profile.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CoachCertificationService {

    private final CoachCertificationApplicationRepository appRepo;
    private final CoachByUserRepository coachRepo;
    private final UserRoleBindingRepository roleRepo;
    private final AppUserRepository userRepo;
    private final UserProfileRepository profileRepo;

    public CoachCertificationService(
        CoachCertificationApplicationRepository appRepo,
        CoachByUserRepository coachRepo,
        UserRoleBindingRepository roleRepo,
        AppUserRepository userRepo,
        UserProfileRepository profileRepo
    ) {
        this.appRepo = appRepo;
        this.coachRepo = coachRepo;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    @Transactional
    public CertificationDto submit(Long userId, SubmitCertificationRequest req) {
        if (!userRepo.existsById(userId)) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        appRepo.findFirstByUserIdAndApplicationStatus(userId, "pending").ifPresent(x -> {
            throw new BizException("CERT_DUPLICATED", "已有待处理的资质申请");
        });
        CoachCertificationApplication app = new CoachCertificationApplication();
        app.setUserId(userId);
        app.setApplicationType(req.applicationType() == null ? "independent" : req.applicationType());
        app.setCoachType(req.coachType() == null ? "freelance" : req.coachType());
        app.setApplicationStatus("pending");
        app.setRemark(req.remark());
        return toDto(appRepo.save(app));
    }

    @Transactional(readOnly = true)
    public List<CertificationDto> mine(Long userId) {
        return appRepo.findByUserIdOrderByIdDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<CertificationDto> listByStatus(String status, int page, int pageSize) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        String st = status == null || status.isBlank() ? "pending" : status;
        return appRepo.findByApplicationStatusOrderByIdAsc(st, PageRequest.of(p - 1, s))
            .map(this::toDto);
    }

    @Transactional
    public CertificationDto approve(Long adminId, Long appId, HandleCertificationRequest req) {
        CoachCertificationApplication app = loadPending(appId);
        app.setApplicationStatus("approved");
        app.setReviewedByUserId(adminId);
        app.setReviewedAt(OffsetDateTime.now());
        if (req != null) app.setReviewRemark(req.remark());

        // 联动：建 coach 行（若无）+ 绑 COACH 角色（若无）
        if (!coachRepo.existsByUserId(app.getUserId())) {
            Coach c = new Coach();
            c.setUserId(app.getUserId());
            c.setDisplayName(deriveDisplayName(app.getUserId()));
            c.setCertificationStatus("approved");
            c.setCoachType(app.getCoachType());
            c.setAvgRating(BigDecimal.ZERO);
            coachRepo.save(c);
        } else {
            coachRepo.findByUserId(app.getUserId()).ifPresent(existing -> {
                if (!"approved".equals(existing.getCertificationStatus())) {
                    existing.setCertificationStatus("approved");
                }
                existing.setCoachType(app.getCoachType());
                coachRepo.save(existing);
            });
        }
        boolean alreadyBound = roleRepo.findByUserIdAndStatus(app.getUserId(), "ACTIVE")
            .stream().map(UserRoleBinding::getRole).anyMatch("COACH"::equals);
        if (!alreadyBound) {
            UserRoleBinding r = new UserRoleBinding();
            r.setUserId(app.getUserId());
            r.setRole("COACH");
            r.setStatus("ACTIVE");
            roleRepo.save(r);
        }
        return toDto(appRepo.save(app));
    }

    @Transactional
    public CertificationDto reject(Long adminId, Long appId, HandleCertificationRequest req) {
        CoachCertificationApplication app = loadPending(appId);
        app.setApplicationStatus("rejected");
        app.setReviewedByUserId(adminId);
        app.setReviewedAt(OffsetDateTime.now());
        if (req != null) app.setReviewRemark(req.remark());
        return toDto(appRepo.save(app));
    }

    private CoachCertificationApplication loadPending(Long id) {
        CoachCertificationApplication app = appRepo.findById(id)
            .orElseThrow(() -> new BizException("CERT_NOT_FOUND", "资质申请不存在"));
        if (!"pending".equals(app.getApplicationStatus())) {
            throw new BizException("CERT_STATE_CONFLICT",
                "申请状态 " + app.getApplicationStatus() + " 不可处理");
        }
        return app;
    }

    private String deriveDisplayName(Long userId) {
        return profileRepo.findById(userId)
            .map(UserProfile::getNickname)
            .filter(s -> s != null && !s.isBlank())
            .orElseGet(() -> userRepo.findById(userId)
                .map(AppUser::getPhone)
                .map(p -> "教练" + p.substring(Math.max(0, p.length() - 4)))
                .orElse("教练" + userId));
    }

    private CertificationDto toDto(CoachCertificationApplication a) {
        return new CertificationDto(
            a.getId(), a.getUserId(), a.getApplicationType(),
            a.getCoachType(),
            a.getApplicationStatus(), a.getRemark(),
            a.getReviewedByUserId(), a.getReviewedAt(), a.getReviewRemark(),
            a.getCreatedAt()
        );
    }
}
