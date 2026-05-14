package com.bitdance.merchant.service;

import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.catalog.repository.StudioRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.UserRoleBinding;
import com.bitdance.iam.repository.UserRoleBindingRepository;
import com.bitdance.merchant.domain.StudioClaim;
import com.bitdance.merchant.dto.HandleClaimRequest;
import com.bitdance.merchant.dto.StudioClaimDto;
import com.bitdance.merchant.dto.SubmitClaimRequest;
import com.bitdance.merchant.repository.StudioClaimRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class StudioClaimService {

    private final StudioClaimRepository claimRepo;
    private final StudioRepository studioRepo;
    private final UserRoleBindingRepository roleRepo;

    public StudioClaimService(
        StudioClaimRepository claimRepo,
        StudioRepository studioRepo,
        UserRoleBindingRepository roleRepo
    ) {
        this.claimRepo = claimRepo;
        this.studioRepo = studioRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public StudioClaimDto submit(Long userId, SubmitClaimRequest req) {
        if (!studioRepo.existsById(req.studioId())) {
            throw new BizException("STUDIO_NOT_FOUND", "舞室不存在");
        }
        claimRepo.findFirstByStudioIdAndApplicantUserIdAndClaimStatus(
            req.studioId(), userId, "pending"
        ).ifPresent(c -> {
            throw new BizException("CLAIM_DUPLICATED", "已有待处理的认领申请");
        });
        StudioClaim c = new StudioClaim();
        c.setStudioId(req.studioId());
        c.setApplicantUserId(userId);
        c.setClaimType(req.claimType() == null ? "owner_claim" : req.claimType());
        c.setBusinessLicenseAssetId(req.businessLicenseAssetId());
        c.setSubmittedRemark(req.submittedRemark());
        c.setClaimStatus("pending");
        return toDto(claimRepo.save(c));
    }

    @Transactional(readOnly = true)
    public List<StudioClaimDto> mine(Long userId) {
        return claimRepo.findByApplicantUserIdOrderByIdDesc(userId).stream()
            .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<StudioClaimDto> listByStatus(String status, int page, int pageSize) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        String st = status == null || status.isBlank() ? "pending" : status;
        return claimRepo.findByClaimStatusOrderByIdAsc(st, PageRequest.of(p - 1, s))
            .map(this::toDto);
    }

    @Transactional
    @AuditAction(value = "studio.claim.approve", targetType = "studio_claim")
    public StudioClaimDto approve(Long adminId, Long claimId, HandleClaimRequest req) {
        StudioClaim c = loadPending(claimId);
        c.setClaimStatus("approved");
        c.setReviewedByUserId(adminId);
        c.setReviewedAt(OffsetDateTime.now());
        if (req != null) c.setReviewRemark(req.remark());
        // 通过后给申请人绑定 STUDIO_ADMIN 角色（避免重复绑定）
        boolean alreadyBound = roleRepo.findByUserIdAndStatus(c.getApplicantUserId(), "ACTIVE")
            .stream().map(UserRoleBinding::getRole).anyMatch("STUDIO_ADMIN"::equals);
        if (!alreadyBound) {
            UserRoleBinding r = new UserRoleBinding();
            r.setUserId(c.getApplicantUserId());
            r.setRole("STUDIO_ADMIN");
            r.setStatus("ACTIVE");
            roleRepo.save(r);
        }
        return toDto(claimRepo.save(c));
    }

    @Transactional
    @AuditAction(value = "studio.claim.reject", targetType = "studio_claim")
    public StudioClaimDto reject(Long adminId, Long claimId, HandleClaimRequest req) {
        StudioClaim c = loadPending(claimId);
        c.setClaimStatus("rejected");
        c.setReviewedByUserId(adminId);
        c.setReviewedAt(OffsetDateTime.now());
        if (req != null) c.setReviewRemark(req.remark());
        return toDto(claimRepo.save(c));
    }

    private StudioClaim loadPending(Long id) {
        StudioClaim c = claimRepo.findById(id)
            .orElseThrow(() -> new BizException("CLAIM_NOT_FOUND", "认领申请不存在"));
        if (!"pending".equals(c.getClaimStatus())) {
            throw new BizException("CLAIM_STATE_CONFLICT",
                "申请状态 " + c.getClaimStatus() + " 不可处理");
        }
        return c;
    }

    private StudioClaimDto toDto(StudioClaim c) {
        return new StudioClaimDto(
            c.getId(), c.getStudioId(), c.getApplicantUserId(),
            c.getClaimType(), c.getClaimStatus(), c.getBusinessLicenseAssetId(),
            c.getSubmittedRemark(), c.getReviewedByUserId(), c.getReviewedAt(),
            c.getReviewRemark(), c.getCreatedAt()
        );
    }
}
