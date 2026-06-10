package com.bitdance.courseorder.service;

import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.domain.CourseSchedule;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.CourseScheduleRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.courseorder.domain.CourseCheckin;
import com.bitdance.courseorder.domain.CourseOrder;
import com.bitdance.courseorder.domain.CourseRefundRequest;
import com.bitdance.courseorder.dto.CheckinCourseOrderRequest;
import com.bitdance.courseorder.dto.CourseOrderDto;
import com.bitdance.courseorder.dto.CourseRefundDto;
import com.bitdance.courseorder.dto.CreateCourseOrderRequest;
import com.bitdance.courseorder.dto.HandleCourseRefundRequest;
import com.bitdance.courseorder.dto.RefundCourseOrderRequest;
import com.bitdance.courseorder.repository.CourseCheckinRepository;
import com.bitdance.courseorder.repository.CourseOrderRepository;
import com.bitdance.courseorder.repository.CourseRefundRequestRepository;
import com.bitdance.merchant.service.MerchantAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CourseOrderService {

    private static final SecureRandom RND = new SecureRandom();

    private final CourseRepository courseRepo;
    private final CourseScheduleRepository scheduleRepo;
    private final CourseOrderRepository orderRepo;
    private final CourseCheckinRepository checkinRepo;
    private final CourseRefundRequestRepository refundRepo;
    private final MerchantAccessGuard guard;

    public CourseOrderService(
        CourseRepository courseRepo,
        CourseScheduleRepository scheduleRepo,
        CourseOrderRepository orderRepo,
        CourseCheckinRepository checkinRepo,
        CourseRefundRequestRepository refundRepo,
        MerchantAccessGuard guard
    ) {
        this.courseRepo = courseRepo;
        this.scheduleRepo = scheduleRepo;
        this.orderRepo = orderRepo;
        this.checkinRepo = checkinRepo;
        this.refundRepo = refundRepo;
        this.guard = guard;
    }

    @Transactional
    public CourseOrderDto create(Long userId, CreateCourseOrderRequest req) {
        Course course = courseRepo.findById(req.courseId())
            .orElseThrow(() -> new BizException("COURSE_NOT_FOUND", "课程不存在"));
        CourseSchedule schedule = scheduleRepo.findById(req.courseScheduleId())
            .orElseThrow(() -> new BizException("SCHEDULE_NOT_FOUND", "课程场次不存在"));
        if (!schedule.getCourseId().equals(course.getId())) {
            throw new BizException("INVALID_ARGUMENT", "场次与课程不匹配");
        }
        if (!"published".equals(course.getStatus())) {
            throw new BizException("COURSE_NOT_PUBLISHED", "课程未发布");
        }
        if (!List.of("scheduled", "open").contains(schedule.getStatus())) {
            throw new BizException("SCHEDULE_NOT_AVAILABLE", "场次不可预约");
        }
        if (OffsetDateTime.now().isAfter(schedule.getStartAt())) {
            throw new BizException("SCHEDULE_STARTED", "场次已开始");
        }
        return orderRepo.findFirstByUserIdAndCourseScheduleIdAndOrderStatusIn(
            userId, schedule.getId(), List.of("pending_payment", "paid", "refund_requested", "checked_in", "completed")
        ).map(this::toDto).orElseGet(() -> {
            CourseOrder o = new CourseOrder();
            o.setOrderNo(generateOrderNo());
            o.setCourseId(course.getId());
            o.setCourseScheduleId(schedule.getId());
            o.setStudioId(schedule.getStudioId());
            o.setCoachId(schedule.getCoachId());
            o.setUserId(userId);
            o.setAmountPayable(course.getPriceAmount());
            return toDto(orderRepo.save(o));
        });
    }

    @Transactional
    public CourseOrderDto pay(Long userId, Long orderId) {
        CourseOrder o = loadOwned(userId, orderId);
        if (!"pending_payment".equals(o.getOrderStatus())) {
            if (List.of("paid", "checked_in", "completed").contains(o.getOrderStatus())) return toDto(o);
            throw new BizException("ORDER_STATE_CONFLICT", "当前订单状态不可支付");
        }
        if (scheduleRepo.tryReserveSeat(o.getCourseScheduleId()) == 0) {
            throw new BizException("COURSE_FULL", "场次名额已满");
        }
        String code = generateCheckinCode();
        o.setOrderStatus("paid");
        o.setAmountPaid(o.getAmountPayable());
        o.setPaymentTxnNo("MOCK-COURSE-" + System.currentTimeMillis());
        o.setPaidAt(OffsetDateTime.now());
        o.setCheckinCode(code);
        CourseOrder saved = orderRepo.save(o);

        CourseCheckin c = new CourseCheckin();
        c.setCourseOrderId(saved.getId());
        c.setCourseScheduleId(saved.getCourseScheduleId());
        c.setCheckinCode(code);
        checkinRepo.save(c);
        return toDto(saved);
    }

    @Transactional
    public CourseOrderDto cancel(Long userId, Long orderId) {
        CourseOrder o = loadOwned(userId, orderId);
        if (!"pending_payment".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT", "仅待支付订单可取消");
        }
        o.setOrderStatus("canceled");
        o.setCanceledAt(OffsetDateTime.now());
        return toDto(orderRepo.save(o));
    }

    @Transactional
    public CourseRefundDto requestRefund(Long userId, Long orderId, RefundCourseOrderRequest req) {
        CourseOrder o = loadOwned(userId, orderId);
        if (!"paid".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT", "仅已支付且未核销订单可申请退款");
        }
        refundRepo.findFirstByCourseOrderIdAndRequestStatus(o.getId(), "pending").ifPresent(x -> {
            throw new BizException("REFUND_DUPLICATED", "已有待审核退款申请");
        });
        o.setOrderStatus("refund_requested");
        o.setRefundRequestedAt(OffsetDateTime.now());
        orderRepo.save(o);

        CourseRefundRequest r = new CourseRefundRequest();
        r.setCourseOrderId(o.getId());
        r.setRequesterUserId(userId);
        r.setRefundReason(req == null ? null : req.reason());
        return toRefundDto(refundRepo.save(r));
    }

    @Transactional(readOnly = true)
    public List<CourseOrderDto> mine(Long userId) {
        return orderRepo.findByUserIdOrderByIdDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseOrderDto> merchantOrders(Long actorId, Long studioId, String status) {
        guard.requireStudioOwnership(actorId, studioId);
        List<CourseOrder> orders = status == null || status.isBlank()
            ? orderRepo.findByStudioIdOrderByIdDesc(studioId)
            : orderRepo.findByStudioIdAndOrderStatusOrderByIdDesc(studioId, status);
        return orders.stream().map(this::toDto).toList();
    }

    @Transactional
    public CourseOrderDto merchantCheckin(Long actorId, Long orderId, CheckinCourseOrderRequest req) {
        CourseOrder o = orderRepo.findById(orderId)
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        guard.requireStudioOwnership(actorId, o.getStudioId());
        return checkinPaidOrder(actorId, o, req == null ? null : req.code());
    }

    @Transactional
    public CourseOrderDto merchantCheckinByCode(Long actorId, String code) {
        CourseOrder o = orderRepo.findByCheckinCode(code)
            .orElseThrow(() -> new BizException("CHECKIN_CODE_INVALID", "核销码不存在"));
        guard.requireStudioOwnership(actorId, o.getStudioId());
        return checkinPaidOrder(actorId, o, code);
    }

    @Transactional(readOnly = true)
    public List<CourseRefundDto> refunds(Long actorId, Long studioId, String status) {
        guard.requireStudioOwnership(actorId, studioId);
        String st = status == null || status.isBlank() ? "pending" : status;
        return refundRepo.findByRequestStatusOrderByIdAsc(st).stream()
            .filter(r -> orderRepo.findById(r.getCourseOrderId())
                .map(o -> o.getStudioId().equals(studioId)).orElse(false))
            .map(this::toRefundDto)
            .toList();
    }

    @Transactional
    public CourseRefundDto approveRefund(Long actorId, Long refundId, HandleCourseRefundRequest req) {
        CourseRefundRequest r = loadPendingRefund(refundId);
        CourseOrder o = orderRepo.findById(r.getCourseOrderId())
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        guard.requireStudioOwnership(actorId, o.getStudioId());
        r.setRequestStatus("approved");
        r.setReviewedByUserId(actorId);
        r.setReviewedAt(OffsetDateTime.now());
        r.setReviewRemark(req == null ? null : req.remark());
        o.setOrderStatus("refunded");
        o.setRefundedAt(OffsetDateTime.now());
        orderRepo.save(o);
        scheduleRepo.releaseSeat(o.getCourseScheduleId());
        checkinRepo.findByCourseOrderId(o.getId()).ifPresent(c -> {
            c.setCheckinStatus("expired");
            checkinRepo.save(c);
        });
        return toRefundDto(refundRepo.save(r));
    }

    @Transactional
    public CourseRefundDto rejectRefund(Long actorId, Long refundId, HandleCourseRefundRequest req) {
        CourseRefundRequest r = loadPendingRefund(refundId);
        CourseOrder o = orderRepo.findById(r.getCourseOrderId())
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        guard.requireStudioOwnership(actorId, o.getStudioId());
        r.setRequestStatus("rejected");
        r.setReviewedByUserId(actorId);
        r.setReviewedAt(OffsetDateTime.now());
        r.setReviewRemark(req == null ? null : req.remark());
        o.setOrderStatus("refund_rejected");
        orderRepo.save(o);
        return toRefundDto(refundRepo.save(r));
    }

    private CourseOrderDto checkinPaidOrder(Long actorId, CourseOrder o, String code) {
        if (!"paid".equals(o.getOrderStatus())) {
            throw new BizException("ORDER_STATE_CONFLICT", "仅已支付订单可核销");
        }
        CourseCheckin c = checkinRepo.findByCourseOrderId(o.getId())
            .orElseThrow(() -> new BizException("CHECKIN_TICKET_NOT_FOUND", "核销码不存在"));
        if (code == null || !c.getCheckinCode().equals(code.trim())) {
            throw new BizException("CHECKIN_CODE_INVALID", "核销码错误");
        }
        if (c.getCheckedInByUserId() != null) {
            return toDto(o);
        }
        CourseSchedule schedule = scheduleRepo.findById(o.getCourseScheduleId())
            .orElseThrow(() -> new BizException("SCHEDULE_NOT_FOUND", "场次不存在"));
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(schedule.getStartAt().minusHours(1))) {
            throw new BizException("CHECKIN_TOO_EARLY", "开课前 1 小时内才可核销");
        }
        if (now.isAfter(schedule.getEndAt())) {
            throw new BizException("CHECKIN_EXPIRED", "场次已结束，核销码失效");
        }
        c.setCheckedInByUserId(actorId);
        c.setCheckedInAt(now);
        c.setCheckinStatus("checked_in");
        checkinRepo.save(c);
        o.setOrderStatus("checked_in");
        o.setCompletedAt(now);
        return toDto(orderRepo.save(o));
    }

    private CourseOrder loadOwned(Long userId, Long id) {
        CourseOrder o = orderRepo.findById(id)
            .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "订单不存在"));
        if (!o.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权操作他人订单");
        }
        return o;
    }

    private CourseRefundRequest loadPendingRefund(Long id) {
        CourseRefundRequest r = refundRepo.findById(id)
            .orElseThrow(() -> new BizException("REFUND_NOT_FOUND", "退款申请不存在"));
        if (!"pending".equals(r.getRequestStatus())) {
            throw new BizException("REFUND_STATE_CONFLICT", "退款申请已处理");
        }
        return r;
    }

    private String generateOrderNo() {
        return "CO" + System.currentTimeMillis() + (RND.nextInt(900000) + 100000);
    }

    private String generateCheckinCode() {
        return String.format("%08d", RND.nextInt(100_000_000));
    }

    private CourseOrderDto toDto(CourseOrder o) {
        return new CourseOrderDto(
            o.getId(), o.getOrderNo(), o.getCourseId(), o.getCourseScheduleId(),
            o.getStudioId(), o.getCoachId(), o.getUserId(),
            o.getAmountPayable(), o.getAmountPaid(), o.getOrderStatus(),
            o.getPaymentTxnNo(), o.getCheckinCode(),
            o.getPaidAt(), o.getCanceledAt(), o.getRefundRequestedAt(),
            o.getRefundedAt(), o.getCompletedAt(), o.getCreatedAt()
        );
    }

    private CourseRefundDto toRefundDto(CourseRefundRequest r) {
        return new CourseRefundDto(
            r.getId(), r.getCourseOrderId(), r.getRequesterUserId(), r.getRefundReason(),
            r.getRequestStatus(), r.getReviewedByUserId(), r.getReviewedAt(),
            r.getReviewRemark(), r.getCreatedAt()
        );
    }
}
