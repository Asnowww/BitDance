package com.bitdance.workshop.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.merchant.service.CoachRelationService;
import com.bitdance.merchant.service.MerchantAccessGuard;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopSession;
import com.bitdance.workshop.dto.CreateSessionRequest;
import com.bitdance.workshop.dto.CreateWorkshopRequest;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.repository.WorkshopRepository;
import com.bitdance.workshop.repository.WorkshopSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MerchantWorkshopService {

    private final WorkshopRepository workshopRepo;
    private final WorkshopSessionRepository sessionRepo;
    private final MerchantAccessGuard guard;
    private final CoachRelationService coachRelationService;

    public MerchantWorkshopService(
        WorkshopRepository workshopRepo,
        WorkshopSessionRepository sessionRepo,
        MerchantAccessGuard guard,
        CoachRelationService coachRelationService
    ) {
        this.workshopRepo = workshopRepo;
        this.sessionRepo = sessionRepo;
        this.guard = guard;
        this.coachRelationService = coachRelationService;
    }

    @Transactional
    public WorkshopDetail create(Long creatorId, CreateWorkshopRequest req) {
        guard.requireStudioOwnership(creatorId, req.studioId());
        if (req.minPeople() != null && req.maxPeople() != null
            && req.maxPeople() < req.minPeople()) {
            throw new BizException("INVALID_ARGUMENT", "最大人数必须不小于最小人数");
        }

        Workshop w = new Workshop();
        w.setCreatorUserId(creatorId);
        w.setStudioId(req.studioId());
        w.setCoachId(req.coachId());
        w.setCityId(req.cityId());
        w.setDanceStyleId(req.danceStyleId());
        w.setWorkshopName(req.workshopName());
        w.setCoverAssetId(req.coverAssetId());
        w.setIntro(req.intro());
        w.setAddress(req.address());
        w.setLocationName(req.locationName());
        w.setLongitude(req.longitude());
        w.setLatitude(req.latitude());
        w.setPriceAmount(req.priceAmount());
        if (req.minPeople() != null) w.setMinPeople(req.minPeople());
        if (req.maxPeople() != null) w.setMaxPeople(req.maxPeople());
        else w.setMaxPeople(w.getMinPeople());
        w.setSignupDeadline(req.signupDeadline());
        w.setSourceType(req.sourceType() == null ? "studio" : req.sourceType());

        // 审核策略：签约/全职教练直发，独立教练或未挂教练走 pending 审核
        boolean autoApprove = req.coachId() == null
            || coachRelationService.isCoachContracted(req.studioId(), req.coachId());
        w.setAuditStatus(autoApprove ? "approved" : "pending");
        w.setPublishStatus("draft");
        return toDetail(workshopRepo.save(w));
    }

    @Transactional
    public WorkshopDetail publish(Long actorId, Long workshopId) {
        Workshop w = loadOwned(actorId, workshopId);
        if (!"approved".equals(w.getAuditStatus())) {
            throw new BizException("WORKSHOP_NOT_APPROVED", "Workshop 未通过审核，无法上架");
        }
        if (!"draft".equals(w.getPublishStatus()) && !"offline".equals(w.getPublishStatus())) {
            throw new BizException("WORKSHOP_STATE_CONFLICT",
                "当前状态 " + w.getPublishStatus() + " 不可上架");
        }
        w.setPublishStatus("published");
        return toDetail(workshopRepo.save(w));
    }

    @Transactional
    public WorkshopDetail offline(Long actorId, Long workshopId) {
        Workshop w = loadOwned(actorId, workshopId);
        if (!"published".equals(w.getPublishStatus())) {
            throw new BizException("WORKSHOP_STATE_CONFLICT",
                "当前状态 " + w.getPublishStatus() + " 不可下架");
        }
        w.setPublishStatus("offline");
        return toDetail(workshopRepo.save(w));
    }

    @Transactional
    public SessionDto addSession(Long actorId, CreateSessionRequest req) {
        Workshop w = loadOwned(actorId, req.workshopId());
        if (!req.endAt().isAfter(req.startAt())) {
            throw new BizException("INVALID_ARGUMENT", "结束时间必须晚于开始时间");
        }
        WorkshopSession s = new WorkshopSession();
        s.setWorkshopId(w.getId());
        s.setSessionName(req.sessionName());
        s.setStartAt(req.startAt());
        s.setEndAt(req.endAt());
        s.setCapacity(req.capacity());
        s.setSessionStatus("scheduled");
        WorkshopSession saved = sessionRepo.save(s);
        return new SessionDto(
            saved.getId(), saved.getWorkshopId(), saved.getSessionName(),
            saved.getStartAt(), saved.getEndAt(),
            saved.getCapacity(), saved.getSoldCount(), saved.getCheckinCount(),
            saved.getSessionStatus()
        );
    }

    private Workshop loadOwned(Long actorId, Long workshopId) {
        Workshop w = workshopRepo.findById(workshopId)
            .orElseThrow(() -> new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        guard.requireStudioOwnership(actorId, w.getStudioId());
        return w;
    }

    private WorkshopDetail toDetail(Workshop w) {
        List<SessionDto> sessions = sessionRepo.findByWorkshopIdOrderByStartAtAsc(w.getId()).stream()
            .map(s -> new SessionDto(
                s.getId(), s.getWorkshopId(), s.getSessionName(),
                s.getStartAt(), s.getEndAt(),
                s.getCapacity(), s.getSoldCount(), s.getCheckinCount(),
                s.getSessionStatus()
            ))
            .toList();
        return new WorkshopDetail(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(), w.getDanceStyleId(),
            w.getWorkshopName(), w.getCoverAssetId(), w.getIntro(),
            w.getAddress(), w.getLocationName(), w.getPriceAmount(),
            w.getMinPeople(), w.getMaxPeople(), w.getSignupDeadline(),
            w.getPublishStatus(), w.getAuditStatus(),
            null, null, java.math.BigDecimal.ZERO,
            w.getLocationName(), w.getAddress(), null, w.getLongitude(), w.getLatitude(),
            0L, java.math.BigDecimal.ZERO, List.of(),
            sessions, false
        );
    }
}
