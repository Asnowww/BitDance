package com.bitdance.coachops.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.repository.CoachByUserRepository;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.CourseScheduleRepository;
import com.bitdance.coachops.dto.CoachDashboardDto;
import com.bitdance.coachops.dto.CoachMeDto;
import com.bitdance.coachops.dto.UpdateCoachProfileRequest;
import com.bitdance.merchant.domain.StudioCoachRelation;
import com.bitdance.merchant.repository.StudioCoachRelationRepository;
import com.bitdance.review.repository.ReviewReplyRepository;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CoachOpsService {

    private final CoachAccessGuard guard;
    private final CoachByUserRepository coachRepo;
    private final StudioCoachRelationRepository relationRepo;
    private final CourseRepository courseRepo;
    private final CourseScheduleRepository scheduleRepo;
    private final ReviewRepository reviewRepo;
    private final ReviewReplyRepository replyRepo;

    public CoachOpsService(
        CoachAccessGuard guard,
        CoachByUserRepository coachRepo,
        StudioCoachRelationRepository relationRepo,
        CourseRepository courseRepo,
        CourseScheduleRepository scheduleRepo,
        ReviewRepository reviewRepo,
        ReviewReplyRepository replyRepo
    ) {
        this.guard = guard;
        this.coachRepo = coachRepo;
        this.relationRepo = relationRepo;
        this.courseRepo = courseRepo;
        this.scheduleRepo = scheduleRepo;
        this.reviewRepo = reviewRepo;
        this.replyRepo = replyRepo;
    }

    @Transactional(readOnly = true)
    public CoachMeDto me(Long userId) {
        return coachRepo.findByUserId(userId).map(c -> {
            List<Long> studios = relationRepo.findByCoachIdAndRelationStatus(c.getId(), "active")
                .stream().map(StudioCoachRelation::getStudioId).toList();
            return new CoachMeDto(
                "approved".equals(c.getCertificationStatus()),
                c.getId(), c.getDisplayName(), c.getIntro(), c.getTeachingStyle(),
                c.getCertificationStatus(), c.getHomeStudioId(), c.getCoverAssetId(),
                c.getAvgRating(), studios
            );
        }).orElse(new CoachMeDto(
            false, null, null, null, null, "not_applied",
            null, null, BigDecimal.ZERO, List.of()
        ));
    }

    @Transactional
    public CoachMeDto updateProfile(Long userId, UpdateCoachProfileRequest req) {
        Coach c = guard.requireCoach(userId);
        if (req.displayName() != null && !req.displayName().isBlank()) {
            c.setDisplayName(req.displayName());
        }
        if (req.intro() != null) c.setIntro(req.intro());
        if (req.teachingStyle() != null) c.setTeachingStyle(req.teachingStyle());
        if (req.coverAssetId() != null) c.setCoverAssetId(req.coverAssetId());
        if (req.homeStudioId() != null) c.setHomeStudioId(req.homeStudioId());
        coachRepo.save(c);
        return me(userId);
    }

    @Transactional(readOnly = true)
    public CoachDashboardDto dashboard(Long userId) {
        Coach c = guard.requireCoach(userId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime monthStart = now.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
        OffsetDateTime monthEnd = monthStart.plusMonths(1);

        long monthSessions = scheduleRepo
            .findByStudioIdAndStartAtBetweenOrderByStartAtAsc(
                c.getHomeStudioId() == null ? -1L : c.getHomeStudioId(),
                monthStart, monthEnd
            )
            .stream()
            .filter(s -> s.getCoachId() != null && s.getCoachId().equals(c.getId()))
            .count();

        // pendingReplies：评价我（target_type=coach, target_id=coach.id）尚未回复的数量
        List<Long> myReviewIds = reviewRepo.findPublishedFor("coach", c.getId())
            .stream().map(r -> r.getId()).toList();
        Set<Long> repliedIds = new HashSet<>();
        for (Long rid : myReviewIds) {
            if (!replyRepo.findByReviewIdOrderByIdAsc(rid).isEmpty()) repliedIds.add(rid);
        }
        long pending = myReviewIds.size() - repliedIds.size();

        return new CoachDashboardDto(
            monthSessions,
            0L, // monthWorkshopOrders 留待 BE-015 接 WorkshopOrderRepository.countByPaidAtBetween
            BigDecimal.ZERO, // monthIncome 留待 settlement_rule 接入
            pending,
            c.getAvgRating(),
            myReviewIds.size()
        );
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> myCourses(Long userId) {
        Coach c = guard.requireCoach(userId);
        return courseRepo.findByCoachIdAndStatusOrderByIdDesc(c.getId(), "published")
            .stream().map(this::toCourseSummary).toList();
    }

    private CourseSummaryDto toCourseSummary(Course c) {
        return new CourseSummaryDto(
            c.getId(), c.getStudioId(), c.getDanceStyleId(),
            c.getCourseName(), c.getDifficultyLevel(),
            c.getPriceAmount(), c.getDurationMinutes(),
            c.getStatus()
        );
    }

    public record CourseSummaryDto(
        Long id,
        Long studioId,
        Long danceStyleId,
        String courseName,
        String difficultyLevel,
        java.math.BigDecimal priceAmount,
        Integer durationMinutes,
        String status
    ) {}
}
