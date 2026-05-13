package com.bitdance.booking.service;

import com.bitdance.booking.domain.TrialBooking;
import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.repository.TrialBookingRepository;
import com.bitdance.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * 试听预约的商家侧状态流转。
 * 数据权限：操作者必须是该 booking.studio_id 的认领管理员（在 BE-014.2 接入 MerchantAccessGuard 后强化）。
 * 本期先做单纯归属与状态校验，跨表数据权限留到 BE-014.2 commit。
 */
@Service
public class MerchantTrialBookingService {

    private static final Set<String> CAN_CONFIRM = Set.of("pending");
    private static final Set<String> CAN_REJECT  = Set.of("pending");
    private static final Set<String> CAN_ATTEND  = Set.of("confirmed");
    private static final Set<String> CAN_NO_SHOW = Set.of("confirmed");

    private final TrialBookingRepository repo;

    public MerchantTrialBookingService(TrialBookingRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public BookingDto confirm(Long actorId, Long bookingId) {
        return transition(actorId, bookingId, "confirmed", CAN_CONFIRM);
    }

    @Transactional
    public BookingDto reject(Long actorId, Long bookingId, String reason) {
        TrialBooking b = load(bookingId);
        requireStatus(b, CAN_REJECT);
        b.setBookingStatus("rejected");
        b.setConfirmedByUserId(actorId);
        if (reason != null) b.setCancelReason(reason);
        return toDto(repo.save(b));
    }

    @Transactional
    public BookingDto attend(Long actorId, Long bookingId) {
        TrialBooking b = load(bookingId);
        requireStatus(b, CAN_ATTEND);
        b.setBookingStatus("attended");
        b.setAttendedAt(OffsetDateTime.now());
        b.setConfirmedByUserId(actorId);
        return toDto(repo.save(b));
    }

    @Transactional
    public BookingDto noShow(Long actorId, Long bookingId) {
        TrialBooking b = load(bookingId);
        requireStatus(b, CAN_NO_SHOW);
        b.setBookingStatus("no_show");
        b.setConfirmedByUserId(actorId);
        return toDto(repo.save(b));
    }

    private BookingDto transition(Long actorId, Long bookingId, String to, Set<String> allowed) {
        TrialBooking b = load(bookingId);
        requireStatus(b, allowed);
        b.setBookingStatus(to);
        b.setConfirmedByUserId(actorId);
        b.setConfirmedAt(OffsetDateTime.now());
        return toDto(repo.save(b));
    }

    private TrialBooking load(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new BizException("BOOKING_NOT_FOUND", "预约不存在"));
    }

    private void requireStatus(TrialBooking b, Set<String> allowed) {
        if (!allowed.contains(b.getBookingStatus())) {
            throw new BizException("BOOKING_STATE_CONFLICT",
                "当前状态 " + b.getBookingStatus() + " 不可执行该操作");
        }
    }

    private BookingDto toDto(TrialBooking b) {
        return new BookingDto(
            b.getId(), b.getUserId(), b.getCourseId(), b.getCourseScheduleId(),
            b.getStudioId(), b.getBookingStatus(),
            b.getContactPhone(), b.getBookingNote(),
            b.getConfirmedAt(), b.getAttendedAt(), b.getCanceledAt(),
            b.getCancelReason(), b.getCreatedAt()
        );
    }
}
