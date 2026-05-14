package com.bitdance.workshop.service;

import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.common.exception.BizException;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.dto.WorkshopAdminItem;
import com.bitdance.workshop.repository.WorkshopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminWorkshopService {

    private final WorkshopRepository workshopRepo;

    public AdminWorkshopService(WorkshopRepository workshopRepo) {
        this.workshopRepo = workshopRepo;
    }

    @Transactional(readOnly = true)
    public Page<WorkshopAdminItem> listByAuditStatus(String auditStatus, int page, int pageSize) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        String st = auditStatus == null || auditStatus.isBlank() ? "pending" : auditStatus;
        return workshopRepo.findByAuditStatusOrderByIdAsc(st, PageRequest.of(p - 1, s))
            .map(this::toItem);
    }

    @Transactional
    @AuditAction(value = "workshop.audit.approve", targetType = "workshop")
    public WorkshopAdminItem approve(Long adminId, Long workshopId) {
        Workshop w = loadPending(workshopId);
        w.setAuditStatus("approved");
        return toItem(workshopRepo.save(w));
    }

    @Transactional
    @AuditAction(value = "workshop.audit.reject", targetType = "workshop")
    public WorkshopAdminItem reject(Long adminId, Long workshopId) {
        Workshop w = loadPending(workshopId);
        w.setAuditStatus("rejected");
        return toItem(workshopRepo.save(w));
    }

    private Workshop loadPending(Long id) {
        Workshop w = workshopRepo.findById(id)
            .orElseThrow(() -> new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        if (!"pending".equals(w.getAuditStatus())) {
            throw new BizException("WORKSHOP_AUDIT_STATE_CONFLICT",
                "审核状态 " + w.getAuditStatus() + " 不可处理");
        }
        return w;
    }

    private WorkshopAdminItem toItem(Workshop w) {
        return new WorkshopAdminItem(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(),
            w.getWorkshopName(), w.getPriceAmount(), w.getSignupDeadline(),
            w.getAuditStatus(), w.getPublishStatus()
        );
    }
}
