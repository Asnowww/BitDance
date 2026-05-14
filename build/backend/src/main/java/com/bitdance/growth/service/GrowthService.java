package com.bitdance.growth.service;

import com.bitdance.badge.service.BadgeRuleEngine;
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
import com.bitdance.growth.dto.GrowthStats;
import com.bitdance.growth.dto.TimelineItem;
import com.bitdance.growth.dto.UpsertGoalRequest;
import com.bitdance.growth.dto.WorkDto;
import com.bitdance.growth.repository.GrowthBadgeRepository;
import com.bitdance.growth.repository.GrowthCheckinRepository;
import com.bitdance.growth.repository.GrowthGoalRepository;
import com.bitdance.growth.repository.GrowthWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GrowthService {

    private final GrowthCheckinRepository checkinRepo;
    private final GrowthGoalRepository goalRepo;
    private final GrowthWorkRepository workRepo;
    private final GrowthBadgeRepository badgeRepo;
    private final BadgeRuleEngine badgeRuleEngine;

    public GrowthService(
        GrowthCheckinRepository checkinRepo,
        GrowthGoalRepository goalRepo,
        GrowthWorkRepository workRepo,
        GrowthBadgeRepository badgeRepo,
        BadgeRuleEngine badgeRuleEngine
    ) {
        this.checkinRepo = checkinRepo;
        this.goalRepo = goalRepo;
        this.workRepo = workRepo;
        this.badgeRepo = badgeRepo;
        this.badgeRuleEngine = badgeRuleEngine;
    }

    // ============ Checkin ============

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

        // 自动更新进行中的目标
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

        // 触发徽章引擎：传入当前用户连续打卡天数与累计天数
        Set<LocalDate> days = new HashSet<>();
        for (GrowthCheckin x : checkinRepo.findByUserIdOrderByCheckinAtDesc(userId)) {
            days.add(x.getCheckinAt().toLocalDate());
        }
        int streak = computeStreak(days);
        badgeRuleEngine.evaluate(userId, "checkin",
            Map.of("streak", streak, "totalCount", days.size()),
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

    // ============ Stats ============

    @Transactional(readOnly = true)
    public GrowthStats stats(Long userId) {
        List<GrowthCheckin> items = checkinRepo.findByUserIdOrderByCheckinAtDesc(userId);
        if (items.isEmpty()) {
            return new GrowthStats(0, 0, 0, 0, 0, null);
        }
        long totalSessions = items.size();
        long totalMinutes = items.stream().mapToLong(GrowthCheckin::getDurationMinutes).sum();
        Set<LocalDate> distinctDays = new HashSet<>();
        Set<Long> styleSet = new HashSet<>();
        for (GrowthCheckin c : items) {
            distinctDays.add(c.getCheckinAt().toLocalDate());
            if (c.getDanceStyleId() != null) styleSet.add(c.getDanceStyleId());
        }
        int streak = computeStreak(distinctDays);
        OffsetDateTime last = items.get(0).getCheckinAt();
        return new GrowthStats(
            totalSessions, totalMinutes, distinctDays.size(),
            styleSet.size(), streak, last
        );
    }

    /** 从今天起回溯，连续打卡天数（含今天即今天有打卡则从今天开始算）。 */
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

    // ============ Timeline ============

    @Transactional(readOnly = true)
    public List<TimelineItem> timeline(Long userId) {
        List<TimelineItem> out = new ArrayList<>();
        for (GrowthCheckin c : checkinRepo.findByUserIdOrderByCheckinAtDesc(userId)) {
            out.add(new TimelineItem(
                "checkin", c.getId(), "训练打卡",
                c.getDurationMinutes() + " 分钟" +
                    (c.getFeelingText() == null ? "" : " · " + truncate(c.getFeelingText(), 30)),
                c.getCheckinAt()
            ));
        }
        for (GrowthWork w : workRepo.findByUserIdOrderByIdDesc(userId)) {
            out.add(new TimelineItem(
                "work", w.getId(), "阶段作品 · " + w.getWorkTitle(),
                w.getWorkDescription() == null ? null : truncate(w.getWorkDescription(), 30),
                w.getCreatedAt() == null ? OffsetDateTime.now() : w.getCreatedAt()
            ));
        }
        // 试听完成、约练完成、评价发布等事件，待 BE-013/BE-014 接事件总线后补
        out.sort(Comparator.comparing(TimelineItem::ts).reversed());
        return out;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    // ============ Goal ============

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
        return goalRepo
            .findFirstByUserIdAndGoalStatusOrderByIdDesc(userId, "active")
            .map(this::toGoalDto)
            .orElse(null);
    }

    // ============ Work ============

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

        long totalWorks = workRepo.findByUserIdOrderByIdDesc(userId).size();
        badgeRuleEngine.evaluate(userId, "work_published",
            Map.of("totalCount", totalWorks),
            "work", saved.getId());

        return toWorkDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkDto> listWorks(Long userId) {
        return workRepo.findByUserIdOrderByIdDesc(userId)
            .stream().map(this::toWorkDto).toList();
    }

    @Transactional
    public void deleteWork(Long userId, Long id) {
        GrowthWork w = workRepo.findById(id)
            .orElseThrow(() -> new BizException("WORK_NOT_FOUND", "作品不存在"));
        if (!w.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人作品");
        }
        workRepo.delete(w);
    }

    // ============ Badge ============

    @Transactional(readOnly = true)
    public List<BadgeDto> listBadges(Long userId) {
        return badgeRepo.findByUserIdOrderByAwardedAtDesc(userId).stream()
            .map(b -> new BadgeDto(b.getId(), b.getBadgeId(), b.getSourceType(),
                b.getSourceRefId(), b.getAwardedAt()))
            .toList();
    }

    // ============ helpers ============

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
            c.getDurationMinutes(), c.getFeelingText(),
            c.getIsPublic(), c.getCheckinAt()
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
        return new WorkDto(
            w.getId(), w.getUserId(), w.getDanceStyleId(),
            w.getWorkTitle(), w.getWorkDescription(),
            w.getCoverAssetId(), w.getIsPublic(), w.getCreatedAt()
        );
    }
}
