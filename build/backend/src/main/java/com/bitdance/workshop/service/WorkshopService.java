package com.bitdance.workshop.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.repository.FavoriteRepository;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopCheckin;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.domain.WorkshopSession;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.CreateOrderRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.dto.RefundRequest;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopBrief;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.dto.WorkshopListResponse;
import com.bitdance.workshop.repository.WorkshopCheckinRepository;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopRepository;
import com.bitdance.workshop.repository.WorkshopSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkshopService {

    private static final String FAV_TARGET = "workshop";
    private static final SecureRandom RND = new SecureRandom();

    private final WorkshopRepository workshopRepo;
    private final WorkshopSessionRepository sessionRepo;
    private final WorkshopOrderRepository orderRepo;
    private final WorkshopCheckinRepository checkinRepo;
    private final FavoriteRepository favoriteRepo;
    private final PaymentGateway paymentGateway;

    public WorkshopService(
        WorkshopRepository workshopRepo,
        WorkshopSessionRepository sessionRepo,
        WorkshopOrderRepository orderRepo,
        WorkshopCheckinRepository checkinRepo,
        FavoriteRepository favoriteRepo,
        PaymentGateway paymentGateway
    ) {
        this.workshopRepo = workshopRepo;
        this.sessionRepo = sessionRepo;
        this.orderRepo = orderRepo;
        this.checkinRepo = checkinRepo;
        this.favoriteRepo = favoriteRepo;
        this.paymentGateway = paymentGateway;
    }

    // ============ Browse ============

    @Transactional(readOnly = true)
    public WorkshopListResponse list(Long cityId, Long danceStyleId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<Workshop> p = workshopRepo.listPublished(
            cityId, danceStyleId, PageRequest.of(safePage - 1, safeSize)
        );
        List<WorkshopBrief> items = p.getContent().stream().map(this::toBrief).toList();
        return new WorkshopListResponse(items, safePage, safeSize, p.getTotalElements());
    }

    @Transactional(readOnly = true)
    public WorkshopDetail detail(Long id, Long currentUserId) {
        Workshop w = workshopRepo.findById(id)
            .orElseThrow(() -> new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        if (!"published".equals(w.getPublishStatus())
            && (currentUserId == null || !currentUserId.equals(w.getCreatorUserId()))) {
            throw new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在");
        }
        List<SessionDto> sessions = sessionRepo.findByWorkshopIdOrderByStartAtAsc(id).stream()
            .map(this::toSessionDto).toList();
        boolean favored = currentUserId != null
            && favoriteRepo.existsByUserIdAndTargetTypeAndTargetId(currentUserId, FAV_TARGET, id);
        return new WorkshopDetail(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(), w.getDanceStyleId(),
            w.getWorkshopName(), w.getCoverAssetId(), w.getIntro(),
            w.getAddress(), w.getLocationName(), w.getPriceAmount(),
            w.getMinPeople(), w.getMaxPeople(), w.getSignupDeadline(),
            w.getPublishStatus(), w.getAuditStatus(),
            sessions, favored
        );
    }

    // ============ Order ============

    @Transactional
    public OrderDto createOrder(Long userId, CreateOrderRequest req) {
        Workshop w = workshopRepo.findById(req.workshopId())
            .orElseThrow(() -> new BizException("WORKSHOP_NOT_FOUND", "Workshop 不存在"));
        if (!"published".equals(w.getPublishStatus())) {
            throw new BizException("WORKSHOP_NOT_PUBLISHED", "Workshop 未上架");
        }
        WorkshopSession session = sessionRepo.findById(req.sessionId())
            .orElseThrow(() -> new BizException("SESSION_NOT_FOUND", "场次不存在"));
        if (!session.getWorkshopId().equals(w.getId())) {
            throw new BizException("INVALID_ARGUMENT", "场次与 Workshop 不匹配");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (w.getSignupDeadline() != null && now.isAfter(w.getSignupDeadline())) {
            throw new BizException("SIGNUP_CLOSED", "报名已截止");
        }
        if (now.isAfter(session.getStartAt())) {
            throw new BizException("SESSION_STARTED", "场次已开始");
        }
        if (!List.of("scheduled", "open").contains(session.getSessionStatus())) {
            throw new BizException("SESSION_NOT_AVAILABLE", "场次不可报名");
        }
        if (session.getSoldCount() >= session.getCapacity()) {
            throw new BizException("WORKSHOP_FULL", "场次已满");
        }

        // 幂等：同一用户同一场次已有未结束订单则直接返回
        Optional<WorkshopOrder> existing = orderRepo.findFirstByUserIdAndWorkshopSessionIdAndOrderStatusIn(
            userId, req.sessionId(), List.of("pending_payment", "paid", "completed")
        );
        if (existing.isPresent()) {
            return toOrderDto(existing.get(), null);
        }

        WorkshopOrder o = new WorkshopOrder();
        o.setOrderNo(generateOrderNo());
        o.setWorkshopId(w.getId());
        o.setWorkshopSessionId(session.getId());
        o.setUserId(userId);
        o.setAmountPayable(w.getPriceAmount());
        o.setOrderStatus("pending_payment");
        return toOrderDto(orderRepo.save(o), null);
    }

    @Transactional
    public OrderDto pay(Long userId, Long orderId) {
        WorkshopOrder o = loadOwned(userId, orderId);
        if (!"pending_payment".equals(o.getOrderStatus())) {
            if ("paid".equals(o.getOrderStatus())) {
                // 幂等：已支付直接返回
                return toOrderDto(o, checkinRepo.findByWorkshopOrderId(o.getId()).orElse(null));
            }
            throw new BizException("ORDER_STATE_CONFLICT",
                "订单状态 " + o.getOrderStatus() + " 不可支付");
        }

        // 先占座：原子 UPDATE，保证不会超卖
        int reserved = sessionRepo.tryReserveSeat(o.getWorkshopSessionId());
        if (reserved == 0) {
            throw new BizException("WORKSHOP_FULL", "场次已满");
        }

        String txnNo;
        try {
            txnNo = paymentGateway.charge(o);
        } catch (RuntimeException ex) {
            // 支付失败释放座位
            sessionRepo.releaseSeat(o.getWorkshopSessionId());
            throw new BizException("PAYMENT_FAILED", "支付失败：" + ex.getMessage());
        }

        o.setOrderStatus("paid");
        o.setAmountPaid(o.getAmountPayable());
        o.setPaymentTxnNo(txnNo);
        o.setPaidAt(OffsetDateTime.now());
        orderRepo.save(o);

        // 生成签到码（写 workshop_checkin 以保留唯一记录）
        WorkshopCheckin c = new WorkshopCheckin();
        c.setWorkshopOrderId(o.getId());
        c.setWorkshopSessionId(o.getWorkshopSessionId());
        c.setCheckinCode(generateCheckinCode());
        c.setCheckinStatus("checked_in"); // 占位状态；真正签到时不改这里
        c.setCheckedInAt(OffsetDateTime.now()); // schema NOT NULL 字段，先填创建时间
        WorkshopCheckin saved = checkinRepo.save(c);
        return toOrderDto(o, saved);
    }

    @Transactional
    public OrderDto cancel(Long userId, Long orderId) {
        WorkshopOrder o = loadOwned(userId, orderId);
        if (!"pending_payment".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT",
                "订单状态 " + o.getOrderStatus() + " 不可取消，请走退款流程");
        }
        o.setOrderStatus("canceled");
        o.setCanceledAt(OffsetDateTime.now());
        return toOrderDto(orderRepo.save(o), null);
    }

    @Transactional
    public OrderDto refund(Long userId, Long orderId, RefundRequest req) {
        WorkshopOrder o = loadOwned(userId, orderId);
        if (!"paid".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT",
                "订单状态 " + o.getOrderStatus() + " 不可退款");
        }
        // 退款前置检查：已签到不允许退款
        WorkshopCheckin existing = checkinRepo.findByWorkshopOrderId(o.getId()).orElse(null);
        if (existing != null && existing.getCheckedInByUserId() != null) {
            throw new BizException("ALREADY_CHECKED_IN", "已签到的订单不可退款");
        }
        o.setOrderStatus("refunded");
        o.setRefundedAt(OffsetDateTime.now());
        if (req != null) o.setRefundReason(req.reason());
        sessionRepo.releaseSeat(o.getWorkshopSessionId());
        return toOrderDto(orderRepo.save(o), existing);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listMyOrders(Long userId) {
        return orderRepo.findByUserIdOrderByIdDesc(userId).stream()
            .map(o -> {
                WorkshopCheckin c = checkinRepo.findByWorkshopOrderId(o.getId()).orElse(null);
                return toOrderDto(o, c);
            })
            .toList();
    }

    // ============ Checkin ============

    @Transactional
    public OrderDto checkin(Long userId, Long orderId, CheckinRequest req) {
        WorkshopOrder o = loadOwned(userId, orderId);
        if (!"paid".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT", "仅已支付订单可签到");
        }
        WorkshopCheckin c = checkinRepo.findByWorkshopOrderId(orderId)
            .orElseThrow(() -> new BizException("CHECKIN_TICKET_NOT_FOUND", "签到码不存在"));
        if (!c.getCheckinCode().equalsIgnoreCase(req.code().trim())) {
            throw new BizException("CHECKIN_CODE_INVALID", "签到码错误");
        }
        if (c.getCheckedInByUserId() != null) {
            // 已签到幂等
            return toOrderDto(o, c);
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
        c.setCheckedInByUserId(userId);
        c.setCheckedInAt(now);
        c.setCheckinStatus("checked_in");
        checkinRepo.save(c);
        sessionRepo.incrementCheckin(session.getId());
        return toOrderDto(o, c);
    }

    // ============ Helpers ============

    private WorkshopOrder loadOwned(Long userId, Long orderId) {
        WorkshopOrder o = orderRepo.findById(orderId)
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        if (!o.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权操作他人订单");
        }
        return o;
    }

    private String generateOrderNo() {
        long ts = System.currentTimeMillis();
        int rnd = RND.nextInt(900_000) + 100_000;
        return "WS" + ts + rnd;
    }

    private String generateCheckinCode() {
        // 8 位字母+数字
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(alphabet.charAt(RND.nextInt(alphabet.length())));
        return sb.toString();
    }

    private WorkshopBrief toBrief(Workshop w) {
        return new WorkshopBrief(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(), w.getDanceStyleId(),
            w.getWorkshopName(), w.getCoverAssetId(), w.getLocationName(),
            w.getPriceAmount(), w.getSignupDeadline(), w.getPublishStatus()
        );
    }

    private SessionDto toSessionDto(WorkshopSession s) {
        return new SessionDto(
            s.getId(), s.getWorkshopId(), s.getSessionName(),
            s.getStartAt(), s.getEndAt(),
            s.getCapacity(), s.getSoldCount(), s.getCheckinCount(),
            s.getSessionStatus()
        );
    }

    private OrderDto toOrderDto(WorkshopOrder o, WorkshopCheckin c) {
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
