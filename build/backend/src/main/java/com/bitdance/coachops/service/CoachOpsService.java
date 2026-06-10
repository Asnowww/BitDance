package com.bitdance.coachops.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.repository.CoachByUserRepository;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.CourseScheduleRepository;
import com.bitdance.coachops.dto.CoachDashboardDto;
import com.bitdance.coachops.dto.CoachMeDto;
import com.bitdance.coachops.dto.UpdateCoachProfileRequest;
import com.bitdance.courseorder.domain.CourseOrder;
import com.bitdance.courseorder.repository.CourseOrderRepository;
import com.bitdance.courseorder.repository.CourseRefundRequestRepository;
import com.bitdance.merchant.domain.StudioCoachRelation;
import com.bitdance.merchant.repository.StudioCoachRelationRepository;
import com.bitdance.merchant.service.MerchantAccessGuard;
import com.bitdance.review.repository.ReviewReplyRepository;
import com.bitdance.review.repository.ReviewRepository;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final CourseOrderRepository courseOrderRepo;
    private final CourseRefundRequestRepository courseRefundRepo;
    private final WorkshopOrderRepository workshopOrderRepo;
    private final WorkshopRepository workshopRepo;
    private final MerchantAccessGuard merchantGuard;

    public CoachOpsService(
        CoachAccessGuard guard,
        CoachByUserRepository coachRepo,
        StudioCoachRelationRepository relationRepo,
        CourseRepository courseRepo,
        CourseScheduleRepository scheduleRepo,
        ReviewRepository reviewRepo,
        ReviewReplyRepository replyRepo,
        CourseOrderRepository courseOrderRepo,
        CourseRefundRequestRepository courseRefundRepo,
        WorkshopOrderRepository workshopOrderRepo,
        WorkshopRepository workshopRepo,
        MerchantAccessGuard merchantGuard
    ) {
        this.guard = guard;
        this.coachRepo = coachRepo;
        this.relationRepo = relationRepo;
        this.courseRepo = courseRepo;
        this.scheduleRepo = scheduleRepo;
        this.reviewRepo = reviewRepo;
        this.replyRepo = replyRepo;
        this.courseOrderRepo = courseOrderRepo;
        this.courseRefundRepo = courseRefundRepo;
        this.workshopOrderRepo = workshopOrderRepo;
        this.workshopRepo = workshopRepo;
        this.merchantGuard = merchantGuard;
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
        if (req.displayName() != null && !req.displayName().isBlank()) c.setDisplayName(req.displayName());
        if (req.intro() != null) c.setIntro(req.intro());
        if (req.teachingStyle() != null) c.setTeachingStyle(req.teachingStyle());
        if (req.coverAssetId() != null) c.setCoverAssetId(req.coverAssetId());
        if (req.homeStudioId() != null) c.setHomeStudioId(req.homeStudioId());
        coachRepo.save(c);
        return me(userId);
    }

    @Transactional(readOnly = true)
    public CoachDashboardDto dashboard(Long userId) {
        return dashboard(userId, null, null);
    }

    @Transactional(readOnly = true)
    public CoachDashboardDto dashboard(Long userId, String role, Long studioId) {
        Coach c = guard.requireCoach(userId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime monthStart = now.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
        OffsetDateTime monthEnd = monthStart.plusMonths(1);
        boolean studioScope = role != null && role.equalsIgnoreCase("studio_admin");
        Long scopedStudioId = studioId == null ? c.getHomeStudioId() : studioId;
        if (studioScope) {
            merchantGuard.requireStudioOwnership(userId, scopedStudioId);
        }

        long monthSessions = scopedStudioId == null ? 0 : scheduleRepo
            .findByStudioIdAndStartAtBetweenOrderByStartAtAsc(scopedStudioId, monthStart, monthEnd)
            .stream()
            .filter(s -> studioScope || Objects.equals(s.getCoachId(), c.getId()))
            .count();

        List<CourseOrder> courseOrders = scopedStudioId == null ? List.of() : courseOrderRepo
            .findByStudioIdAndPaidAtBetween(scopedStudioId, monthStart, monthEnd)
            .stream()
            .filter(o -> studioScope || Objects.equals(o.getCoachId(), c.getId()))
            .toList();
        long courseBookings = courseOrders.size();
        long courseCheckins = courseOrders.stream()
            .filter(o -> List.of("checked_in", "completed").contains(o.getOrderStatus()))
            .count();
        BigDecimal courseIncome = courseOrders.stream()
            .filter(o -> List.of("paid", "checked_in", "completed").contains(o.getOrderStatus()))
            .map(CourseOrder::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WorkshopOrder> workshopOrders = workshopOrderRepo.findByPaidAtBetween(monthStart, monthEnd).stream()
            .filter(o -> workshopRepo.findById(o.getWorkshopId()).map(w ->
                studioScope ? Objects.equals(w.getStudioId(), scopedStudioId)
                    : Objects.equals(w.getCoachId(), c.getId()) || Objects.equals(w.getCreatorUserId(), userId)
            ).orElse(false))
            .toList();
        long workshopSignups = workshopOrders.size();
        BigDecimal workshopIncome = workshopOrders.stream()
            .filter(o -> List.of("paid", "completed").contains(o.getOrderStatus()))
            .map(WorkshopOrder::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long workshopRefunds = workshopOrders.stream().filter(o -> "refunded".equals(o.getOrderStatus())).count();
        long courseRefunds = courseRefundRepo.countByRequestStatusAndCreatedAtBetween("approved", monthStart, monthEnd);

        List<Long> reviewIds = (studioScope && scopedStudioId != null
            ? reviewRepo.findPublishedFor("studio", scopedStudioId)
            : reviewRepo.findPublishedFor("coach", c.getId()))
            .stream().map(r -> r.getId()).toList();
        Set<Long> repliedIds = new HashSet<>();
        for (Long rid : reviewIds) {
            if (!replyRepo.findByReviewIdOrderByIdAsc(rid).isEmpty()) repliedIds.add(rid);
        }
        long pendingReplies = reviewIds.size() - repliedIds.size();

        return new CoachDashboardDto(
            courseIncome.add(workshopIncome),
            courseBookings + workshopSignups,
            courseCheckins,
            courseRefunds + workshopRefunds,
            courseBookings,
            workshopSignups,
            pendingReplies,
            c.getAvgRating(),
            monthSessions,
            workshopSignups,
            reviewIds.size()
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
        BigDecimal priceAmount,
        Integer durationMinutes,
        String status
    ) {}
}
