package com.bitdance.workshop.service;

import com.bitdance.badge.service.BadgeRuleEngine;
import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.domain.Studio;
import com.bitdance.catalog.repository.CoachRepository;
import com.bitdance.catalog.repository.StudioRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.repository.FavoriteRepository;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.review.domain.Review;
import com.bitdance.review.repository.ReviewRepository;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopCheckin;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.domain.WorkshopSession;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.CreateOrderRequest;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.dto.RefundRequest;
import com.bitdance.workshop.dto.SessionDto;
import com.bitdance.workshop.dto.WorkshopCalendarEventDto;
import com.bitdance.workshop.dto.WorkshopBrief;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.dto.WorkshopListResponse;
import com.bitdance.workshop.dto.WorkshopReviewSnippet;
import com.bitdance.workshop.repository.WorkshopCheckinRepository;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopRepository;
import com.bitdance.workshop.repository.WorkshopSessionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CoachRepository coachRepo;
    private final StudioRepository studioRepo;
    private final ReviewRepository reviewRepo;
    private final AppUserRepository userRepo;
    private final NotificationRepository notificationRepo;
    private final PaymentGateway paymentGateway;
    private final BadgeRuleEngine badgeRuleEngine;

    public WorkshopService(
        WorkshopRepository workshopRepo,
        WorkshopSessionRepository sessionRepo,
        WorkshopOrderRepository orderRepo,
        WorkshopCheckinRepository checkinRepo,
        FavoriteRepository favoriteRepo,
        CoachRepository coachRepo,
        StudioRepository studioRepo,
        ReviewRepository reviewRepo,
        AppUserRepository userRepo,
        NotificationRepository notificationRepo,
        PaymentGateway paymentGateway,
        BadgeRuleEngine badgeRuleEngine
    ) {
        this.workshopRepo = workshopRepo;
        this.sessionRepo = sessionRepo;
        this.orderRepo = orderRepo;
        this.checkinRepo = checkinRepo;
        this.favoriteRepo = favoriteRepo;
        this.coachRepo = coachRepo;
        this.studioRepo = studioRepo;
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.notificationRepo = notificationRepo;
        this.paymentGateway = paymentGateway;
        this.badgeRuleEngine = badgeRuleEngine;
    }

    // ============ Browse ============

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "workshop:list",
        key = "(#cityId == null ? 'all' : #cityId) + ':' + (#danceStyleId == null ? 'all' : #danceStyleId) + ':' + #page + ':' + #pageSize"
    )
    public WorkshopListResponse list(Long cityId, Long danceStyleId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<Workshop> p = workshopRepo.listPublished(
            cityId, danceStyleId, PageRequest.of(safePage - 1, safeSize)
        );
        List<WorkshopBrief> items = p.getContent().stream()
            .map(this::toBrief)
            .toList();
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
        Coach coach = w.getCoachId() == null ? null : coachRepo.findById(w.getCoachId()).orElse(null);
        Studio studio = w.getStudioId() == null ? null : studioRepo.findById(w.getStudioId()).orElse(null);
        List<Review> reviews = reviewRepo.findPublishedFor("workshop", id);
        List<WorkshopReviewSnippet> pastReviews = reviews.stream()
            .sorted(Comparator.comparing(Review::getPublishedAt).reversed())
            .limit(6)
            .map(this::toReviewSnippet)
            .toList();
        BigDecimal reviewAverage = reviews.isEmpty()
            ? BigDecimal.ZERO
            : reviews.stream()
                .map(Review::getOverallScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()), 2, java.math.RoundingMode.HALF_UP);
        return new WorkshopDetail(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(), w.getDanceStyleId(),
            w.getWorkshopName(), w.getCoverAssetId(), w.getIntro(),
            w.getAddress(), w.getLocationName(), w.getPriceAmount(),
            w.getMinPeople(), w.getMaxPeople(), w.getSignupDeadline(),
            w.getPublishStatus(), w.getAuditStatus(),
            coach == null ? fallbackCoachName(w.getCoachId()) : coach.getDisplayName(),
            coach == null ? null : coach.getIntro(),
            coach == null ? BigDecimal.ZERO : coach.getAvgRating(),
            studio == null ? w.getLocationName() : studio.getStudioName(),
            studio == null ? w.getAddress() : studio.getAddress(),
            studio == null ? null : studio.getTransportInfo(),
            w.getLongitude(), w.getLatitude(),
            (long) reviews.size(), reviewAverage, pastReviews,
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
        Optional<WorkshopOrder> existing = orderRepo.findFirstByUserIdAndWorkshopIdAndOrderStatusIn(
            userId, w.getId(), List.of("pending_payment", "paid", "checked_in", "completed")
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

        // 生成签到凭证；真正扫码成功后再标记为 checked_in。
        WorkshopCheckin c = new WorkshopCheckin();
        c.setWorkshopOrderId(o.getId());
        c.setWorkshopSessionId(o.getWorkshopSessionId());
        c.setCheckinCode(generateCheckinCode());
        c.setCheckinStatus("pending");
        c.setCheckedInAt(OffsetDateTime.now()); // schema NOT NULL 字段，先记录凭证创建时间
        WorkshopCheckin saved = checkinRepo.save(c);
        createNotification(
            userId, "workshop_payment_success", "workshop_order",
            o.getId(), "报名成功",
            "你已成功报名 Workshop，签到码已生成并加入活动日历。"
        );
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
        createNotification(
            userId, "workshop_order_canceled", "workshop_order",
            o.getId(), "订单已取消",
            "未支付订单已取消，本次名额已释放。"
        );
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
        createNotification(
            userId, "workshop_refunded", "workshop_order",
            o.getId(), "退款成功",
            "你的 Workshop 订单已退款，活动名额已释放。"
        );
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

    @Transactional(readOnly = true)
    public OrderDto getMyOrder(Long userId, Long orderId) {
        WorkshopOrder o = loadOwned(userId, orderId);
        return toOrderDto(o, checkinRepo.findByWorkshopOrderId(o.getId()).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<WorkshopCalendarEventDto> listMyCalendar(Long userId) {
        List<WorkshopOrder> orders = orderRepo.findByUserIdAndOrderStatusInOrderByIdDesc(
            userId, List.of("paid", "checked_in", "completed")
        );
        if (orders.isEmpty()) return List.of();
        Map<Long, WorkshopCheckin> checkins = checkinRepo.findByWorkshopOrderIdIn(
            orders.stream().map(WorkshopOrder::getId).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(WorkshopCheckin::getWorkshopOrderId, x -> x));

        return orders.stream()
            .map(order -> toCalendarEvent(order, checkins.get(order.getId())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(WorkshopCalendarEventDto::startAt))
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
        o.setOrderStatus("checked_in");
        orderRepo.save(o);
        sessionRepo.incrementCheckin(session.getId());

        // 触发徽章引擎（用户自助签到）
        long total = orderRepo.findByUserIdOrderByIdDesc(userId).stream()
            .filter(x -> List.of("paid", "checked_in", "completed").contains(x.getOrderStatus()))
            .count();
        badgeRuleEngine.evaluate(userId, "workshop_attended",
            Map.of("totalCount", total),
            "workshop_order", o.getId());

        createNotification(
            userId, "workshop_checkin_success", "workshop_order",
            o.getId(), "签到成功",
            "已完成到场签到，祝你今天跳得尽兴。"
        );

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
        WorkshopSession nextSession = sessionRepo.findByWorkshopIdOrderByStartAtAsc(w.getId()).stream()
            .filter(session -> List.of("scheduled", "open").contains(session.getSessionStatus()))
            .findFirst()
            .orElse(null);
        return new WorkshopBrief(
            w.getId(), w.getStudioId(), w.getCoachId(), w.getCityId(), w.getDanceStyleId(),
            w.getWorkshopName(), w.getCoverAssetId(), w.getLocationName(),
            w.getPriceAmount(), w.getSignupDeadline(), w.getPublishStatus(),
            nextSession == null ? null : nextSession.getStartAt(),
            nextSession == null ? null : nextSession.getEndAt(),
            nextSession == null ? 0 : nextSession.getCapacity(),
            nextSession == null ? 0 : nextSession.getSoldCount()
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

    private Optional<WorkshopCalendarEventDto> toCalendarEvent(WorkshopOrder order, WorkshopCheckin checkin) {
        Workshop workshop = workshopRepo.findById(order.getWorkshopId()).orElse(null);
        WorkshopSession session = sessionRepo.findById(order.getWorkshopSessionId()).orElse(null);
        if (workshop == null || session == null) return Optional.empty();
        Coach coach = workshop.getCoachId() == null ? null : coachRepo.findById(workshop.getCoachId()).orElse(null);
        OffsetDateTime now = OffsetDateTime.now();
        String reminderStage = "";
        String reminderTitle = "";
        String reminderBody = "";
        if (now.isAfter(session.getEndAt())) {
            reminderStage = "ended";
            reminderTitle = "活动已结束";
            reminderBody = "别忘了回到订单页补充你的评价与复盘。";
        } else if (now.isAfter(session.getStartAt().minusHours(1))) {
            reminderStage = "starting_soon";
            reminderTitle = "即将开场";
            reminderBody = "已进入签到时间，打开签到页出示你的签到码。";
        } else if (now.isAfter(session.getStartAt().minusHours(24))) {
            reminderStage = "tomorrow";
            reminderTitle = "明日开跳";
            reminderBody = "活动已加入你的日历，记得提前安排出发时间。";
        } else {
            reminderStage = "upcoming";
            reminderTitle = "已加入日历";
            reminderBody = "后续会在开场前继续提醒你。";
        }
        return Optional.of(new WorkshopCalendarEventDto(
            order.getId(),
            workshop.getId(),
            session.getId(),
            workshop.getWorkshopName(),
            coach == null ? fallbackCoachName(workshop.getCoachId()) : coach.getDisplayName(),
            workshop.getLocationName(),
            workshop.getAddress(),
            order.getOrderStatus(),
            order.getAmountPaid(),
            checkin == null ? null : checkin.getCheckinCode(),
            session.getStartAt(),
            session.getEndAt(),
            reminderStage,
            reminderTitle,
            reminderBody,
            now.isAfter(session.getStartAt().minusHours(1)) && now.isBefore(session.getEndAt())
        ));
    }

    private WorkshopReviewSnippet toReviewSnippet(Review review) {
        AppUser author = userRepo.findById(review.getUserId()).orElse(null);
        return new WorkshopReviewSnippet(
            review.getId(),
            review.getUserId(),
            author == null ? fallbackUserName(review.getUserId()) : fallbackUserName(author.getId()),
            review.getOverallScore(),
            review.getContentText(),
            review.getIsVerified(),
            review.getPublishedAt()
        );
    }

    private String fallbackCoachName(Long coachId) {
        return coachId == null ? "特邀导师" : "导师 #" + coachId;
    }

    private String fallbackUserName(Long userId) {
        if (userId == null) return "舞者";
        String tail = String.valueOf(userId);
        tail = tail.length() <= 4 ? tail : tail.substring(tail.length() - 4);
        return "舞者" + tail;
    }

    private void createNotification(
        Long userId, String noticeType, String targetType,
        Long targetId, String title, String content
    ) {
        if (notificationRepo.existsByUserIdAndNoticeTypeAndTargetTypeAndTargetId(
            userId, noticeType, targetType, targetId
        )) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNoticeType(noticeType);
        notification.setCategory("workshop");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setIsRead(false);
        notification.setSentAt(OffsetDateTime.now());
        notificationRepo.save(notification);
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
