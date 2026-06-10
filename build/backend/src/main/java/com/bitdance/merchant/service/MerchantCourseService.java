package com.bitdance.merchant.service;

import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.domain.CourseSchedule;
import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.CourseScheduleRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.courseorder.dto.CourseOrderDto;
import com.bitdance.courseorder.repository.CourseOrderRepository;
import com.bitdance.merchant.dto.MerchantCourseDto;
import com.bitdance.merchant.dto.MerchantCourseRequest;
import com.bitdance.merchant.dto.MerchantScheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MerchantCourseService {

    private final MerchantAccessGuard guard;
    private final CourseRepository courseRepo;
    private final CourseScheduleRepository scheduleRepo;
    private final CourseOrderRepository orderRepo;

    public MerchantCourseService(
        MerchantAccessGuard guard,
        CourseRepository courseRepo,
        CourseScheduleRepository scheduleRepo,
        CourseOrderRepository orderRepo
    ) {
        this.guard = guard;
        this.courseRepo = courseRepo;
        this.scheduleRepo = scheduleRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public MerchantCourseDto create(Long actorId, MerchantCourseRequest req) {
        guard.requireStudioOwnership(actorId, req.studioId());
        Course c = new Course();
        apply(c, req);
        c.setStatus("draft");
        return toDto(courseRepo.save(c));
    }

    @Transactional
    public MerchantCourseDto update(Long actorId, Long id, MerchantCourseRequest req) {
        Course c = loadCourseForStudio(actorId, id);
        if (!c.getStudioId().equals(req.studioId())) {
            throw new BizException("INVALID_ARGUMENT", "不可修改课程所属舞室");
        }
        apply(c, req);
        return toDto(courseRepo.save(c));
    }

    @Transactional
    public MerchantCourseDto publish(Long actorId, Long id) {
        Course c = loadCourseForStudio(actorId, id);
        c.setStatus("published");
        return toDto(courseRepo.save(c));
    }

    @Transactional
    public MerchantCourseDto offline(Long actorId, Long id) {
        Course c = loadCourseForStudio(actorId, id);
        c.setStatus("offline");
        return toDto(courseRepo.save(c));
    }

    @Transactional(readOnly = true)
    public List<MerchantCourseDto> list(Long actorId, Long studioId, String status) {
        guard.requireStudioOwnership(actorId, studioId);
        List<Course> courses = status == null || status.isBlank()
            ? courseRepo.findByStudioIdOrderByIdDesc(studioId)
            : courseRepo.findByStudioIdAndStatusOrderByIdDesc(studioId, status);
        return courses.stream().map(this::toDto).toList();
    }

    @Transactional
    public ScheduleItem createSchedule(Long actorId, MerchantScheduleRequest req) {
        guard.requireStudioOwnership(actorId, req.studioId());
        Course course = courseRepo.findById(req.courseId())
            .orElseThrow(() -> new BizException("COURSE_NOT_FOUND", "课程不存在"));
        if (!course.getStudioId().equals(req.studioId())) {
            throw new BizException("INVALID_ARGUMENT", "课程与舞室不匹配");
        }
        if (!req.endAt().isAfter(req.startAt())) {
            throw new BizException("INVALID_ARGUMENT", "结束时间必须晚于开始时间");
        }
        CourseSchedule s = new CourseSchedule();
        applySchedule(s, req);
        s.setStatus("scheduled");
        return toScheduleDto(scheduleRepo.save(s));
    }

    @Transactional
    public ScheduleItem updateSchedule(Long actorId, Long id, MerchantScheduleRequest req) {
        CourseSchedule s = scheduleRepo.findById(id)
            .orElseThrow(() -> new BizException("SCHEDULE_NOT_FOUND", "场次不存在"));
        guard.requireStudioOwnership(actorId, s.getStudioId());
        if (!s.getStudioId().equals(req.studioId())) {
            throw new BizException("INVALID_ARGUMENT", "不可修改场次所属舞室");
        }
        applySchedule(s, req);
        return toScheduleDto(scheduleRepo.save(s));
    }

    @Transactional
    public ScheduleItem cancelSchedule(Long actorId, Long id) {
        CourseSchedule s = scheduleRepo.findById(id)
            .orElseThrow(() -> new BizException("SCHEDULE_NOT_FOUND", "场次不存在"));
        guard.requireStudioOwnership(actorId, s.getStudioId());
        s.setStatus("canceled");
        return toScheduleDto(scheduleRepo.save(s));
    }

    @Transactional(readOnly = true)
    public List<ScheduleItem> week(Long actorId, Long studioId, OffsetDateTime from, OffsetDateTime to) {
        guard.requireStudioOwnership(actorId, studioId);
        return scheduleRepo.findByStudioIdAndStartAtBetweenOrderByStartAtAsc(studioId, from, to)
            .stream().map(this::toScheduleDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseOrderDto> bookings(Long actorId, Long scheduleId) {
        CourseSchedule s = scheduleRepo.findById(scheduleId)
            .orElseThrow(() -> new BizException("SCHEDULE_NOT_FOUND", "场次不存在"));
        guard.requireStudioOwnership(actorId, s.getStudioId());
        return orderRepo.findByStudioIdOrderByIdDesc(s.getStudioId()).stream()
            .filter(o -> o.getCourseScheduleId().equals(scheduleId))
            .map(o -> new CourseOrderDto(
                o.getId(), o.getOrderNo(), o.getCourseId(), o.getCourseScheduleId(),
                o.getStudioId(), o.getCoachId(), o.getUserId(), o.getAmountPayable(),
                o.getAmountPaid(), o.getOrderStatus(), o.getPaymentTxnNo(), o.getCheckinCode(),
                o.getPaidAt(), o.getCanceledAt(), o.getRefundRequestedAt(), o.getRefundedAt(),
                o.getCompletedAt(), o.getCreatedAt()
            ))
            .toList();
    }

    private Course loadCourseForStudio(Long actorId, Long id) {
        Course c = courseRepo.findById(id)
            .orElseThrow(() -> new BizException("COURSE_NOT_FOUND", "课程不存在"));
        guard.requireStudioOwnership(actorId, c.getStudioId());
        return c;
    }

    private void apply(Course c, MerchantCourseRequest req) {
        c.setStudioId(req.studioId());
        c.setCoachId(req.coachId());
        c.setDanceStyleId(req.danceStyleId());
        c.setCourseName(req.courseName());
        c.setDifficultyLevel(req.difficultyLevel());
        c.setPriceAmount(req.priceAmount() == null ? BigDecimal.ZERO : req.priceAmount());
        c.setTrialEnabled(Boolean.TRUE.equals(req.trialEnabled()));
        c.setTrialPriceAmount(req.trialPriceAmount());
        c.setTrialCapacity(req.trialCapacity());
        c.setDurationMinutes(req.durationMinutes() == null ? 60 : req.durationMinutes());
        c.setIntensityLevel(req.intensityLevel());
        c.setCourseType(req.courseType() == null || req.courseType().isBlank() ? "regular" : req.courseType());
        c.setZeroBasicFriendly(Boolean.TRUE.equals(req.zeroBasicFriendly()));
        c.setDescription(req.description());
        c.setCoverAssetId(req.coverAssetId());
    }

    private void applySchedule(CourseSchedule s, MerchantScheduleRequest req) {
        s.setCourseId(req.courseId());
        s.setStudioId(req.studioId());
        s.setCoachId(req.coachId());
        s.setClassroomName(req.classroomName());
        s.setStartAt(req.startAt());
        s.setEndAt(req.endAt());
        s.setCapacity(req.capacity());
    }

    private MerchantCourseDto toDto(Course c) {
        return new MerchantCourseDto(
            c.getId(), c.getStudioId(), c.getCoachId(), c.getDanceStyleId(),
            c.getCourseName(), c.getDifficultyLevel(), c.getPriceAmount(),
            c.getTrialEnabled(), c.getTrialPriceAmount(), c.getTrialCapacity(),
            c.getDurationMinutes(), c.getIntensityLevel(), c.getCourseType(),
            c.getZeroBasicFriendly(), c.getDescription(), c.getCoverAssetId(), c.getStatus()
        );
    }

    private ScheduleItem toScheduleDto(CourseSchedule s) {
        return new ScheduleItem(
            s.getId(), s.getCourseId(), s.getStudioId(), s.getCoachId(),
            s.getClassroomName(), s.getStartAt(), s.getEndAt(),
            s.getCapacity(), s.getBookedCount(), s.getStatus()
        );
    }
}
