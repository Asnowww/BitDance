package com.bitdance.booking.service;

import com.bitdance.booking.domain.TrialBooking;
import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.dto.CreateBookingRequest;
import com.bitdance.booking.repository.TrialBookingRepository;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TrialBookingService {

    /** 用户已下未结束的状态，用于防止重复预约同一场次。 */
    private static final List<String> OPEN_STATUSES = List.of("pending", "confirmed");

    /** 用户侧可取消的状态。 */
    private static final Set<String> CANCELABLE = Set.of("pending", "confirmed");

    private final TrialBookingRepository bookingRepo;
    private final CourseRepository courseRepo;

    public TrialBookingService(TrialBookingRepository bookingRepo, CourseRepository courseRepo) {
        this.bookingRepo = bookingRepo;
        this.courseRepo = courseRepo;
    }

    @Transactional
    public BookingDto create(Long userId, CreateBookingRequest req) {
        Course course = courseRepo.findById(req.courseId())
            .orElseThrow(() -> new BizException("COURSE_NOT_FOUND", "课程不存在"));
        if (!"published".equals(course.getStatus())) {
            throw new BizException("COURSE_OFFLINE", "课程不可预约");
        }

        boolean dup = req.courseScheduleId() != null
            ? bookingRepo.existsByUserIdAndCourseIdAndCourseScheduleIdAndBookingStatusIn(
                userId, req.courseId(), req.courseScheduleId(), OPEN_STATUSES)
            : bookingRepo.existsByUserIdAndCourseIdAndBookingStatusIn(
                userId, req.courseId(), OPEN_STATUSES);
        if (dup) {
            throw new BizException("BOOKING_DUPLICATED", "已有未完结的同课程预约");
        }

        TrialBooking b = new TrialBooking();
        b.setUserId(userId);
        b.setCourseId(req.courseId());
        b.setCourseScheduleId(req.courseScheduleId());
        b.setStudioId(course.getStudioId());
        b.setBookingStatus("pending");
        b.setContactPhone(req.contactPhone());
        b.setBookingNote(req.bookingNote());
        return toDto(bookingRepo.save(b));
    }

    @Transactional
    public BookingDto cancel(Long userId, Long bookingId, String reason) {
        TrialBooking b = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new BizException("BOOKING_NOT_FOUND", "预约不存在"));
        if (!b.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权操作他人预约");
        }
        if (!CANCELABLE.contains(b.getBookingStatus())) {
            throw new BizException("BOOKING_STATE_CONFLICT",
                "当前状态 " + b.getBookingStatus() + " 不可取消");
        }
        b.setBookingStatus("canceled");
        b.setCanceledAt(OffsetDateTime.now());
        if (reason != null) b.setCancelReason(reason);
        return toDto(bookingRepo.save(b));
    }

    @Transactional(readOnly = true)
    public List<BookingDto> listMine(Long userId) {
        return bookingRepo.findByUserIdOrderByIdDesc(userId).stream()
            .map(this::toDto).toList();
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
