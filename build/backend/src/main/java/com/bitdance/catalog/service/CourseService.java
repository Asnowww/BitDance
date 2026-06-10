package com.bitdance.catalog.service;

import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.dto.CourseDetail;
import com.bitdance.catalog.dto.ScheduleItem;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.CourseScheduleRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class CourseService {

    private static final String TARGET_TYPE = "course";

    private final CourseRepository courseRepo;
    private final CourseScheduleRepository scheduleRepo;
    private final FavoriteRepository favoriteRepo;

    public CourseService(
        CourseRepository courseRepo,
        CourseScheduleRepository scheduleRepo,
        FavoriteRepository favoriteRepo
    ) {
        this.courseRepo = courseRepo;
        this.scheduleRepo = scheduleRepo;
        this.favoriteRepo = favoriteRepo;
    }

    @Transactional(readOnly = true)
    public CourseDetail detail(Long id, Long currentUserId) {
        Course c = courseRepo.findById(id)
            .orElseThrow(() -> new BizException("COURSE_NOT_FOUND", "课程不存在"));
        if ("offline".equals(c.getStatus())) {
            throw new BizException("COURSE_OFFLINE", "课程已下架");
        }
        boolean favored = currentUserId != null && favoriteRepo
            .existsByUserIdAndTargetTypeAndTargetId(currentUserId, TARGET_TYPE, id);
        return new CourseDetail(
            c.getId(), c.getStudioId(), c.getCoachId(), c.getDanceStyleId(),
            c.getCourseName(), c.getDifficultyLevel(), formatTargetAudience(c.getTargetAudience()),
            c.getPriceAmount(), c.getDurationMinutes(), c.getIntensityLevel(),
            c.getCourseType(), c.getZeroBasicFriendly(), c.getDescription(),
            c.getCoverAssetId(), c.getStatus(), favored
        );
    }

    @Transactional(readOnly = true)
    public List<ScheduleItem> schedulesOfCourse(Long courseId, LocalDate from, LocalDate to) {
        Range r = resolveRange(from, to);
        return scheduleRepo
            .findByCourseIdAndStartAtBetweenOrderByStartAtAsc(courseId, r.from, r.to)
            .stream().map(this::toItem).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleItem> schedulesOfStudio(Long studioId, LocalDate from, LocalDate to) {
        Range r = resolveRange(from, to);
        return scheduleRepo
            .findByStudioIdAndStartAtBetweenOrderByStartAtAsc(studioId, r.from, r.to)
            .stream().map(this::toItem).toList();
    }

    private Range resolveRange(LocalDate from, LocalDate to) {
        LocalDate f = from != null ? from : LocalDate.now();
        LocalDate t = to != null ? to : f.plusDays(6);
        if (t.isBefore(f)) {
            throw new BizException("INVALID_ARGUMENT", "结束日期不能早于开始日期");
        }
        if (f.plusDays(60).isBefore(t)) {
            throw new BizException("INVALID_ARGUMENT", "查询区间不能超过 60 天");
        }
        return new Range(
            f.atStartOfDay().atOffset(ZoneOffset.UTC),
            t.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        );
    }

    private ScheduleItem toItem(com.bitdance.catalog.domain.CourseSchedule s) {
        return new ScheduleItem(
            s.getId(), s.getCourseId(), s.getStudioId(), s.getCoachId(),
            s.getClassroomName(), s.getStartAt(), s.getEndAt(),
            s.getCapacity(), s.getBookedCount(), s.getStatus()
        );
    }

    private String formatTargetAudience(String[] targetAudience) {
        return targetAudience == null ? "" : String.join("、", targetAudience);
    }

    private record Range(OffsetDateTime from, OffsetDateTime to) {}
}
