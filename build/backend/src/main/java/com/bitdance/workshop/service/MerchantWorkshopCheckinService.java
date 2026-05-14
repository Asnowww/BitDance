package com.bitdance.workshop.service;

import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.common.exception.BizException;
import com.bitdance.merchant.service.MerchantAccessGuard;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopCheckin;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.domain.WorkshopSession;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.repository.WorkshopCheckinRepository;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopRepository;
import com.bitdance.workshop.repository.WorkshopSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class MerchantWorkshopCheckinService {

    private final MerchantAccessGuard guard;
    private final WorkshopOrderRepository orderRepo;
    private final WorkshopRepository workshopRepo;
    private final WorkshopSessionRepository sessionRepo;
    private final WorkshopCheckinRepository checkinRepo;

    public MerchantWorkshopCheckinService(
        MerchantAccessGuard guard,
        WorkshopOrderRepository orderRepo,
        WorkshopRepository workshopRepo,
        WorkshopSessionRepository sessionRepo,
        WorkshopCheckinRepository checkinRepo
    ) {
        this.guard = guard;
        this.orderRepo = orderRepo;
        this.workshopRepo = workshopRepo;
        this.sessionRepo = sessionRepo;
        this.checkinRepo = checkinRepo;
    }

    @Transactional
    @AuditAction(value = "workshop.checkin.merchant", targetType = "workshop_order")
    public OrderDto checkin(Long actorId, Long orderId, String code) {
        WorkshopOrder o = orderRepo.findById(orderId)
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        Workshop w = workshopRepo.findById(o.getWorkshopId())
            .orElseThrow(() -> new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        guard.requireStudioOwnership(actorId, w.getStudioId());

        if (!"paid".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT", "仅已支付订单可核销");
        }
        WorkshopCheckin c = checkinRepo.findByWorkshopOrderId(orderId)
            .orElseThrow(() -> new BizException("CHECKIN_TICKET_NOT_FOUND", "签到码不存在"));
        if (code == null || !c.getCheckinCode().equalsIgnoreCase(code.trim())) {
            throw new BizException("CHECKIN_CODE_INVALID", "签到码错误");
        }
        if (c.getCheckedInByUserId() != null) {
            // 幂等：已签到直接返回
            return buildOrderDto(o, c);
        }
        WorkshopSession session = sessionRepo.findById(o.getWorkshopSessionId())
            .orElseThrow(() -> new BizException("SESSION_NOT_FOUND", "场次不存在"));
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(session.getStartAt().minusHours(1))) {
            throw new BizException("CHECKIN_TOO_EARLY", "签到时间未到（开课前 1 小时内）");
        }
        if (now.isAfter(session.getEndAt())) {
            throw new BizException("CHECKIN_TOO_LATE", "签到时间已过");
        }
        c.setCheckedInByUserId(actorId);
        c.setCheckedInAt(now);
        c.setCheckinStatus("manual_checked_in"); // 区分用户自助 checked_in 与商家代办
        checkinRepo.save(c);
        sessionRepo.incrementCheckin(session.getId());
        return buildOrderDto(o, c);
    }

    private OrderDto buildOrderDto(WorkshopOrder o, WorkshopCheckin c) {
        return new OrderDto(
            o.getId(), o.getOrderNo(), o.getWorkshopId(), o.getWorkshopSessionId(),
            o.getUserId(), o.getAmountPayable(), o.getAmountPaid(),
            o.getOrderStatus(), o.getPaymentTxnNo(),
            c == null ? null : c.getCheckinCode(),
            o.getPaidAt(), o.getCanceledAt(), o.getRefundedAt(),
            o.getCreatedAt()
        );
    }
}
