package com.bitdance.merchant.service;

import com.bitdance.catalog.repository.CoachRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.merchant.domain.StudioCoachRelation;
import com.bitdance.merchant.dto.InviteCoachRequest;
import com.bitdance.merchant.dto.StudioCoachRelationDto;
import com.bitdance.merchant.dto.UpdateCoachRelationRequest;
import com.bitdance.merchant.repository.StudioCoachRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CoachRelationService {

    private final StudioCoachRelationRepository relRepo;
    private final CoachRepository coachRepo;
    private final MerchantAccessGuard guard;

    public CoachRelationService(
        StudioCoachRelationRepository relRepo,
        CoachRepository coachRepo,
        MerchantAccessGuard guard
    ) {
        this.relRepo = relRepo;
        this.coachRepo = coachRepo;
        this.guard = guard;
    }

    @Transactional
    public StudioCoachRelationDto invite(Long inviterId, InviteCoachRequest req) {
        guard.requireStudioOwnership(inviterId, req.studioId());
        if (!coachRepo.existsById(req.coachId())) {
            throw new BizException("COACH_NOT_FOUND", "教练不存在");
        }
        // 防止重复邀请（已有 pending/active 关系则拒绝）
        relRepo.findFirstByStudioIdAndCoachIdAndRelationStatusIn(
            req.studioId(), req.coachId(), List.of("pending", "active")
        ).ifPresent(r -> {
            throw new BizException("RELATION_DUPLICATED", "已有进行中的合作关系");
        });
        StudioCoachRelation r = new StudioCoachRelation();
        r.setStudioId(req.studioId());
        r.setCoachId(req.coachId());
        r.setRelationType(req.relationType());
        r.setRelationStatus("pending");
        r.setSettlementMode(req.settlementMode() == null ? "ratio" : req.settlementMode());
        r.setSettlementRatio(req.settlementRatio() == null ? BigDecimal.ZERO : req.settlementRatio());
        r.setInvitedByUserId(inviterId);
        r.setEffectiveFrom(req.effectiveFrom() == null ? LocalDate.now() : req.effectiveFrom());
        r.setEffectiveTo(req.effectiveTo());
        return toDto(relRepo.save(r));
    }

    @Transactional
    public StudioCoachRelationDto update(Long actorId, Long relationId, UpdateCoachRelationRequest req) {
        StudioCoachRelation r = relRepo.findById(relationId)
            .orElseThrow(() -> new BizException("RELATION_NOT_FOUND", "合作关系不存在"));
        guard.requireStudioOwnership(actorId, r.getStudioId());
        if (req.relationStatus() != null) {
            r.setRelationStatus(req.relationStatus());
            if ("active".equals(req.relationStatus())) {
                r.setApprovedByUserId(actorId);
            }
        }
        if (req.relationType() != null) r.setRelationType(req.relationType());
        if (req.settlementRatio() != null) r.setSettlementRatio(req.settlementRatio());
        if (req.effectiveTo() != null) r.setEffectiveTo(req.effectiveTo());
        return toDto(relRepo.save(r));
    }

    @Transactional(readOnly = true)
    public List<StudioCoachRelationDto> listByStudio(Long actorId, Long studioId) {
        guard.requireStudioOwnership(actorId, studioId);
        return relRepo.findByStudioIdOrderByIdDesc(studioId).stream().map(this::toDto).toList();
    }

    /** 给 MerchantWorkshopService 调用，判定 coach 在该 studio 是否签约。 */
    public boolean isCoachContracted(Long studioId, Long coachId) {
        return relRepo.findFirstByStudioIdAndCoachIdAndRelationStatusIn(
            studioId, coachId, List.of("active")
        ).filter(r -> List.of("full_time", "signed").contains(r.getRelationType()))
        .isPresent();
    }

    private StudioCoachRelationDto toDto(StudioCoachRelation r) {
        return new StudioCoachRelationDto(
            r.getId(), r.getStudioId(), r.getCoachId(),
            r.getRelationType(), r.getRelationStatus(),
            r.getSettlementMode(), r.getSettlementRatio(),
            r.getInvitedByUserId(), r.getApprovedByUserId(),
            r.getEffectiveFrom(), r.getEffectiveTo(),
            r.getCreatedAt()
        );
    }
}
