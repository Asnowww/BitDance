package com.bitdance.growth.service;

import com.bitdance.badge.service.BadgeRuleEngine;
import com.bitdance.booking.domain.TrialBooking;
import com.bitdance.booking.repository.TrialBookingRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.growth.domain.GrowthBadge;
import com.bitdance.growth.domain.GrowthCheckin;
import com.bitdance.growth.domain.GrowthGoal;
import com.bitdance.growth.domain.GrowthWork;
import com.bitdance.growth.dto.BadgeDto;
import com.bitdance.growth.dto.CheckinDto;
import com.bitdance.growth.dto.CreateCheckinRequest;
import com.bitdance.growth.dto.CreateWorkRequest;
import com.bitdance.growth.dto.GoalDto;
import com.bitdance.growth.dto.GrowthReportDto;
import com.bitdance.growth.dto.GrowthStats;
import com.bitdance.growth.dto.TimelineItem;
import com.bitdance.growth.dto.UpsertGoalRequest;
import com.bitdance.growth.dto.WorkDto;
import com.bitdance.growth.repository.GrowthBadgeRepository;
import com.bitdance.growth.repository.GrowthCheckinRepository;
import com.bitdance.growth.repository.GrowthGoalRepository;
import com.bitdance.growth.repository.GrowthWorkRepository;
import com.bitdance.media.domain.MediaAsset;
import com.bitdance.media.domain.MediaAttachment;
import com.bitdance.media.dto.MediaAssetDto;
import com.bitdance.media.repository.MediaAssetRepository;
import com.bitdance.media.repository.MediaAttachmentRepository;
import com.bitdance.media.service.MediaAssetService;
import com.bitdance.practice.domain.PracticePost;
import com.bitdance.practice.repository.PracticePostRepository;
import com.bitdance.review.domain.Review;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GrowthService {

    private final GrowthCheckinRepository checkinRepo;
    private final GrowthGoalRepository goalRepo;
    private final GrowthWorkRepository workRepo;
    private final GrowthBadgeRepository badgeRepo;
    private final BadgeRuleEngine badgeRuleEngine;
    private final MediaAttachmentRepository attachmentRepo;
    private final MediaAssetRepository mediaAssetRepo;
    private final MediaAssetService mediaAssetService;
    private final PracticePostRepository practicePostRepo;
    private final ReviewRepository reviewRepo;
    private final TrialBookingRepository trialBookingRepo;

    public GrowthService(
        GrowthCheckinRepository checkinRepo,
        GrowthGoalRepository goalRepo,
        GrowthWorkRepository workRepo,
        GrowthBadgeRepository badgeRepo,
        BadgeRuleEngine badgeRuleEngine,
        MediaAttachmentRepository attachmentRepo,
        MediaAssetRepository mediaAssetRepo,
        MediaAssetService mediaAssetService,
        PracticePostRepository practicePostRepo,
        ReviewRepository reviewRepo,
        TrialBookingRepository trialBookingRepo
    ) {
        this.checkinRepo = checkinRepo;
        this.goalRepo = goalRepo;
        this.workRepo = workRepo;
        this.badgeRepo = badgeRepo;
        this.badgeRuleEngine = badgeRuleEngine;
        this.attachmentRepo = attachmentRepo;
        this.mediaAssetRepo = mediaAssetRepo;
        this.mediaAssetService = mediaAssetService;
        this.practicePostRepo = practicePostRepo;
        this.reviewRepo = reviewRepo;
        this.trialBookingRepo = trialBookingRepo;
    }

    @Transactional
    public CheckinDto createCheckin(Long userId, CreateCheckinRequest req) {
        validateDate(req.checkinAt());
        GrowthCheckin c = new GrowthCheckin();
        c.setUserId(userId);
        c.setDanceStyleId(req.danceStyleId());
        c.setStudioId(req.studioId());
        c.setCourseScheduleId(req.courseScheduleId());
        c.setPracticePostId(req.practicePostId());
        c.setDurationMinutes(req.durationMinutes());
        c.setFeelingText(req.feelingText());
        c.setIsPublic(req.isPublic() == null ? Boolean.TRUE : req.isPublic());
        c.setCheckinAt(req.checkinAt() == null ? OffsetDateTime.now() : req.checkinAt());
        GrowthCheckin saved = checkinRepo.save(c);

        LocalDate day = saved.getCheckinAt().toLocalDate();
        goalRepo
            .findFirstByUserIdAndGoalStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdDesc(
                userId, "active", day, day
            )
            .ifPresent(g -> {
                g.setCurrentMinutes(g.getCurrentMinutes() + saved.getDurationMinutes());
                g.setCurrentTimes(g.getCurrentTimes() + 1);
                if (g.getCurrentMinutes() >= g.getTargetMinutes()
                    && g.getCurrentTimes() >= g.getTargetTimes()
                    && g.getTargetMinutes() > 0
                    && g.getTargetTimes() > 0) {
                    g.setGoalStatus("completed");
                }
                goalRepo.save(g);
            });

        Set<LocalDate> days = new HashSet<>();
        for (GrowthCheckin x : checkinRepo.findByUserIdOrderByCheckinAtDesc(userId)) {
            days.add(x.getCheckinAt().toLocalDate());
        }
        badgeRuleEngine.evaluate(userId, "checkin",
            Map.of("streak", computeStreak(days), "totalCount", days.size()),
            "checkin", saved.getId());
        return toCheckinDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CheckinDto> listCheckins(Long userId) {
        return checkinRepo.findByUserIdOrderByCheckinAtDesc(userId)
            .stream().map(this::toCheckinDto).toList();
    }

    @Transactional
    public void deleteCheckin(Long userId, Long id) {
        GrowthCheckin c = checkinRepo.findById(id)
            .orElseThrow(() -> new BizException("CHECKIN_NOT_FOUND", "打卡不存在"));
        if (!c.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人打卡");
        }
        checkinRepo.delete(c);
    }

    @Transactional(readOnly = true)
    public GrowthStats stats(Long userId) {
        List<GrowthCheckin> items = checkinRepo.findByUserIdOrderByCheckinAtDesc(userId);
        if (items.isEmpty()) {
            return new GrowthStats(0, 0, 0, 0, 0, null, 0, 0, 0, 0, 0);
        }
        long totalMinutes = items.stream().mapToLong(GrowthCheckin::getDurationMinutes).sum();
        Set<LocalDate> distinctDays = new HashSet<>();
        Set<Long> styleSet = new HashSet<>();
        Set<Long> courseSet = new HashSet<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate monthStart = today.withDayOfMonth(1);
        long weekSessions = 0;
        long weekMinutes = 0;
        long monthSessions = 0;
        long monthMinutes = 0;
        for (GrowthCheckin c : items) {
            LocalDate day = c.getCheckinAt().toLocalDate();
            distinctDays.add(day);
            if (c.getDanceStyleId() != null) styleSet.add(c.getDanceStyleId());
            if (c.getCourseScheduleId() != null) courseSet.add(c.getCourseScheduleId());
            if (!day.isBefore(weekStart) && !day.isAfter(today)) {
                weekSessions++;
                weekMinutes += c.getDurationMinutes();
            }
            if (!day.isBefore(monthStart) && !day.isAfter(today)) {
                monthSessions++;
                monthMinutes += c.getDurationMinutes();
            }
        }
        return new GrowthStats(
            items.size(), totalMinutes, distinctDays.size(), styleSet.size(), computeStreak(distinctDays),
            items.get(0).getCheckinAt(), courseSet.size(), weekSessions, weekMinutes, monthSessions, monthMinutes
        );
    }

    @Transactional(readOnly = true)
    public List<TimelineItem> timeline(Long userId) {
        List<TimelineItem> out = new ArrayList<>();
        for (GrowthCheckin c : checkinRepo.findByUserIdOrderByCheckinAtDesc(userId)) {
            addTimelineItem(out,
                "checkin", c.getId(), "训练打卡",
                c.getDurationMinutes() + " 分钟" +
                    (c.getFeelingText() == null ? "" : " · " + truncate(c.getFeelingText(), 30)),
                c.getCheckinAt()
            );
        }
        for (GrowthWork w : workRepo.findByUserIdOrderByIdDesc(userId)) {
            addTimelineItem(out,
                "work", w.getId(), "阶段作品 · " + w.getWorkTitle(),
                w.getWorkDescription() == null ? null : truncate(w.getWorkDescription(), 30),
                w.getCreatedAt() == null ? OffsetDateTime.now() : w.getCreatedAt()
            );
        }
        for (PracticePost p : practicePostRepo.findByCreatorUserIdAndPostStatusOrderByStartAtDesc(userId, "completed")) {
            addTimelineItem(out,
                "practice", p.getId(), "约练完成",
                (p.getSkillLevel() == null ? "" : p.getSkillLevel() + " · ") + p.getLocationName(),
                p.getEndAt()
            );
        }
        for (Review r : reviewRepo.findByUserIdAndReviewStatusOrderByPublishedAtDesc(
            userId, "published", PageRequest.of(0, 50)
        )) {
            addTimelineItem(out,
                "review", r.getId(), "发布评价",
                r.getTargetType() + " #" + r.getTargetId() + " · " + r.getOverallScore() + " 分",
                r.getPublishedAt()
            );
        }
        for (TrialBooking b : trialBookingRepo.findByUserIdAndBookingStatusOrderByAttendedAtDesc(userId, "attended")) {
            addTimelineItem(out,
                "trial", b.getId(), "试听完成",
                "课程 #" + b.getCourseId() + " · 舞室 #" + b.getStudioId(),
                b.getAttendedAt() == null ? b.getCreatedAt() : b.getAttendedAt()
            );
        }
        out.sort(Comparator.comparing(TimelineItem::ts).reversed());
        return out;
    }

    @Transactional
    public GoalDto upsertActiveGoal(Long userId, UpsertGoalRequest req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BizException("INVALID_ARGUMENT", "结束日期不能早于开始日期");
        }
        if (req.goalPeriod() == null) {
            throw new BizException("INVALID_ARGUMENT", "goalPeriod 必填");
        }
        GrowthGoal g = goalRepo
            .findFirstByUserIdAndGoalStatusOrderByIdDesc(userId, "active")
            .orElseGet(GrowthGoal::new);
        g.setUserId(userId);
        g.setGoalPeriod(req.goalPeriod());
        g.setTargetMinutes(req.targetMinutes() == null ? 0 : req.targetMinutes());
        g.setTargetTimes(req.targetTimes() == null ? 0 : req.targetTimes());
        g.setStartDate(req.startDate());
        g.setEndDate(req.endDate());
        g.setGoalStatus("active");
        if (g.getId() == null) {
            g.setCurrentMinutes(0);
            g.setCurrentTimes(0);
        }
        return toGoalDto(goalRepo.save(g));
    }

    @Transactional(readOnly = true)
    public GoalDto activeGoal(Long userId) {
        return goalRepo.findFirstByUserIdAndGoalStatusOrderByIdDesc(userId, "active")
            .map(this::toGoalDto)
            .orElse(null);
    }

    @Transactional
    public WorkDto createWork(Long userId, CreateWorkRequest req) {
        GrowthWork w = new GrowthWork();
        w.setUserId(userId);
        w.setDanceStyleId(req.danceStyleId());
        w.setWorkTitle(req.workTitle());
        w.setWorkDescription(req.workDescription());
        w.setCoverAssetId(req.coverAssetId());
        w.setIsPublic(req.isPublic() == null ? Boolean.TRUE : req.isPublic());
        GrowthWork saved = workRepo.save(w);
        attachWorkMedia(saved, req.mediaAssetIds());

        long totalWorks = workRepo.findByUserIdOrderByIdDesc(userId).size();
        badgeRuleEngine.evaluate(userId, "work_published",
            Map.of("totalCount", totalWorks), "work", saved.getId());
        return toWorkDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkDto> listWorks(Long userId) {
        return workRepo.findByUserIdOrderByIdDesc(userId).stream()
            .map(this::toWorkDto).toList();
    }

    @Transactional
    public void deleteWork(Long userId, Long id) {
        GrowthWork w = workRepo.findById(id)
            .orElseThrow(() -> new BizException("WORK_NOT_FOUND", "作品不存在"));
        if (!w.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人作品");
        }
        attachmentRepo.deleteByTargetTypeAndTargetId("growth_work", id);
        workRepo.delete(w);
    }

    @Transactional(readOnly = true)
    public List<BadgeDto> listBadges(Long userId) {
        return badgeRepo.findByUserIdOrderByAwardedAtDesc(userId).stream()
            .map(b -> new BadgeDto(b.getId(), b.getBadgeId(), b.getSourceType(),
                b.getSourceRefId(), b.getAwardedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public GrowthReportDto report(Long userId, String period, LocalDate anchorDate) {
        String p = (period == null || period.isBlank()) ? "monthly" : period;
        if (!Set.of("monthly", "quarterly").contains(p)) {
            throw new BizException("INVALID_ARGUMENT", "period 必须是 monthly/quarterly");
        }
        LocalDate anchor = anchorDate == null ? LocalDate.now() : anchorDate;
        LocalDate start = "quarterly".equals(p)
            ? anchor.withMonth(((anchor.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1)
            : anchor.withDayOfMonth(1);
        LocalDate end = "quarterly".equals(p) ? start.plusMonths(3).minusDays(1) : start.plusMonths(1).minusDays(1);
        OffsetDateTime from = start.atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        OffsetDateTime to = end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8)).minusNanos(1);

        List<GrowthCheckin> checkins = checkinRepo.findByUserIdAndCheckinAtBetween(userId, from, to);
        List<GrowthWork> works = workRepo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, from, to);
        List<GrowthBadge> badges = badgeRepo.findByUserIdOrderByAwardedAtDesc(userId).stream()
            .filter(b -> b.getAwardedAt() != null && !b.getAwardedAt().isBefore(from) && !b.getAwardedAt().isAfter(to))
            .toList();
        GrowthGoal goal = goalRepo.findFirstByUserIdAndGoalStatusOrderByIdDesc(userId, "active").orElse(null);
        long totalMinutes = checkins.stream().mapToLong(GrowthCheckin::getDurationMinutes).sum();
        Set<LocalDate> activeDays = checkins.stream().map(x -> x.getCheckinAt().toLocalDate()).collect(Collectors.toSet());
        Set<Long> styleSet = checkins.stream().map(GrowthCheckin::getDanceStyleId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Long> styleSessions = checkins.stream()
            .map(GrowthCheckin::getDanceStyleId)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        double goalProgress = goalProgress(goal);
        String suggestion = totalMinutes >= 600
            ? "本周期训练量很稳定，可以把重点放到作品复盘和动作质量。"
            : "建议固定每周 2-3 次短训练，把目标拆成更容易完成的小节奏。";
        return new GrowthReportDto(
            p, start, end, checkins.size(), totalMinutes, activeDays.size(), styleSet.size(),
            works.size(), badges.size(),
            goal == null ? null : goal.getTargetTimes(),
            goal == null ? null : goal.getCurrentTimes(),
            goal == null ? null : goal.getTargetMinutes(),
            goal == null ? null : goal.getCurrentMinutes(),
            goalProgress,
            styleSessions,
            timeline(userId).stream()
                .filter(x -> !x.ts().isBefore(from) && !x.ts().isAfter(to))
                .limit(8)
                .toList(),
            suggestion
        );
    }

    private void addTimelineItem(
        List<TimelineItem> out,
        String type,
        Long refId,
        String title,
        String subtitle,
        OffsetDateTime ts
    ) {
        if (ts == null) return;
        out.add(new TimelineItem(type, refId, title, subtitle, ts));
    }

    private int computeStreak(Set<LocalDate> days) {
        if (days.isEmpty()) return 0;
        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor)) {
            cursor = cursor.minusDays(1);
            if (!days.contains(cursor)) return 0;
        }
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private void validateDate(OffsetDateTime ts) {
        if (ts == null) return;
        if (ts.isAfter(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1))) {
            throw new BizException("INVALID_ARGUMENT", "打卡时间不能晚于现在");
        }
    }

    private CheckinDto toCheckinDto(GrowthCheckin c) {
        return new CheckinDto(
            c.getId(), c.getUserId(), c.getDanceStyleId(),
            c.getStudioId(), c.getCourseScheduleId(), c.getPracticePostId(),
            c.getDurationMinutes(), c.getFeelingText(), c.getIsPublic(), c.getCheckinAt()
        );
    }

    private GoalDto toGoalDto(GrowthGoal g) {
        return new GoalDto(
            g.getId(), g.getUserId(), g.getGoalPeriod(),
            g.getTargetMinutes(), g.getTargetTimes(),
            g.getCurrentMinutes(), g.getCurrentTimes(),
            g.getStartDate(), g.getEndDate(), g.getGoalStatus()
        );
    }

    private WorkDto toWorkDto(GrowthWork w) {
        List<MediaAssetDto> media = workMedia(w.getId());
        return new WorkDto(
            w.getId(), w.getUserId(), w.getDanceStyleId(),
            w.getWorkTitle(), w.getWorkDescription(),
            w.getCoverAssetId(), w.getIsPublic(), w.getCreatedAt(),
            w.getCoverAssetId() == null ? null : mediaAssetService.url(w.getCoverAssetId()),
            media
        );
    }

    private void attachWorkMedia(GrowthWork work, List<Long> mediaAssetIds) {
        if (mediaAssetIds == null || mediaAssetIds.isEmpty()) return;
        List<Long> ids = mediaAssetIds.stream().filter(Objects::nonNull).distinct().limit(9).toList();
        if (ids.isEmpty()) return;
        List<MediaAsset> assets = mediaAssetRepo.findByIdIn(ids);
        if (assets.size() != ids.size()) {
            throw new BizException("MEDIA_NOT_FOUND", "作品媒体不存在");
        }
        int order = 0;
        for (Long assetId : ids) {
            MediaAttachment a = new MediaAttachment();
            a.setAssetId(assetId);
            a.setTargetType("growth_work");
            a.setTargetId(work.getId());
            a.setUsageType(assetId.equals(work.getCoverAssetId()) ? "cover" : "work_media");
            a.setSortOrder(order++);
            attachmentRepo.save(a);
        }
    }

    private List<MediaAssetDto> workMedia(Long workId) {
        List<MediaAttachment> attachments = attachmentRepo
            .findByTargetTypeAndTargetIdOrderBySortOrderAsc("growth_work", workId);
        if (attachments.isEmpty()) return List.of();
        Map<Long, MediaAsset> assets = mediaAssetRepo.findByIdIn(
                attachments.stream().map(MediaAttachment::getAssetId).toList()
            ).stream().collect(Collectors.toMap(MediaAsset::getId, Function.identity()));
        return attachments.stream()
            .map(a -> assets.get(a.getAssetId()))
            .filter(Objects::nonNull)
            .map(mediaAssetService::toDto)
            .toList();
    }

    private double goalProgress(GrowthGoal goal) {
        if (goal == null) return 0;
        double byTimes = goal.getTargetTimes() == 0 ? 0 : (double) goal.getCurrentTimes() / goal.getTargetTimes();
        double byMinutes = goal.getTargetMinutes() == 0 ? 0 : (double) goal.getCurrentMinutes() / goal.getTargetMinutes();
        return Math.min(1.0, Math.max(byTimes, byMinutes));
    }
}
