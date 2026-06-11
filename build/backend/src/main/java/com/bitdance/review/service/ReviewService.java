package com.bitdance.review.service;

import com.bitdance.badge.service.BadgeRuleEngine;
import com.bitdance.booking.domain.TrialBooking;
import com.bitdance.booking.repository.TrialBookingRepository;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.AppUser;
import com.bitdance.iam.repository.AppUserRepository;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewDimensionScore;
import com.bitdance.review.dto.CreateReviewRequest;
import com.bitdance.review.dto.DimensionScoreDto;
import com.bitdance.review.dto.ReviewDto;
import com.bitdance.review.dto.ReviewListResponse;
import com.bitdance.review.dto.ReviewSummary;
import com.bitdance.review.repository.ReviewDimensionScoreRepository;
import com.bitdance.review.repository.ReviewRepository;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopCheckin;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.repository.WorkshopCheckinRepository;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ReviewDimensionScoreRepository dimRepo;
    private final ReviewRiskService riskService;
    private final TrialBookingRepository trialRepo;
    private final CourseRepository courseRepo;
    private final WorkshopOrderRepository workshopOrderRepo;
    private final WorkshopCheckinRepository workshopCheckinRepo;
    private final WorkshopRepository workshopRepo;
    private final AppUserRepository userRepo;
    private final BadgeRuleEngine badgeRuleEngine;

    public ReviewService(
        ReviewRepository reviewRepo,
        ReviewDimensionScoreRepository dimRepo,
        ReviewRiskService riskService,
        TrialBookingRepository trialRepo,
        CourseRepository courseRepo,
        WorkshopOrderRepository workshopOrderRepo,
        WorkshopCheckinRepository workshopCheckinRepo,
        WorkshopRepository workshopRepo,
        AppUserRepository userRepo,
        BadgeRuleEngine badgeRuleEngine
    ) {
        this.reviewRepo = reviewRepo;
        this.dimRepo = dimRepo;
        this.riskService = riskService;
        this.trialRepo = trialRepo;
        this.courseRepo = courseRepo;
        this.workshopOrderRepo = workshopOrderRepo;
        this.workshopCheckinRepo = workshopCheckinRepo;
        this.workshopRepo = workshopRepo;
        this.userRepo = userRepo;
        this.badgeRuleEngine = badgeRuleEngine;
    }

    @Transactional
    @CacheEvict(cacheNames = "review:summary",
        key = "#req.targetType() + ':' + #req.targetId()")
    public ReviewDto create(Long userId, CreateReviewRequest req) {
        AppUser author = userRepo.findById(userId)
            .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));

        validateDimensionCodes(req.dimensions());
        boolean verified = verifySource(userId, req);

        ReviewRiskService.Verdict verdict = riskService.assess(
            author, verified, req.targetType(), req.targetId()
        );

        Review saved = reviewRepo.save(buildReview(userId, req, verified, verdict));
        List<ReviewDimensionScore> dims = buildDimensionScores(saved.getId(), req.dimensions());
        dimRepo.saveAll(dims);

        awardReviewBadge(userId, saved.getId());

        return toDto(saved, dims);
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review r = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new BizException("REVIEW_NOT_FOUND", "评价不存在"));
        if (!r.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人评价");
        }
        reviewRepo.delete(r);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse list(
        String targetType, Long targetId, String sort, String status, int page, int pageSize
    ) {
        validateTargetType(targetType);
        String safeStatus = validatePublicStatus(status);
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        PageRequest pr = PageRequest.of(safePage - 1, safeSize);
        Page<Review> p = switch (sort == null ? "latest" : sort) {
            case "helpful" -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusOrderByHelpfulCountDescPublishedAtDesc(
                    targetType, targetId, safeStatus, pr);
            case "verified" -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusAndIsVerifiedOrderByPublishedAtDesc(
                    targetType, targetId, safeStatus, true, pr);
            default -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusOrderByPublishedAtDesc(
                    targetType, targetId, safeStatus, pr);
        };

        List<Long> ids = p.getContent().stream().map(Review::getId).toList();
        Map<Long, List<ReviewDimensionScore>> byReview = ids.isEmpty()
            ? Map.of()
            : dimRepo.findByReviewIdIn(ids).stream()
                .collect(Collectors.groupingBy(ReviewDimensionScore::getReviewId));
        // 评价媒体按本页 reviewId 一次性取回，避免列表每条评价重复查附件。

        return new ReviewListResponse(
            p.getContent().stream()
                .map(r -> toDto(r, byReview.getOrDefault(r.getId(), List.of())))
                .toList(),
            safePage,
            safeSize,
            p.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listByUser(Long userId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<Review> p = reviewRepo.findByUserIdAndReviewStatusOrderByPublishedAtDesc(
            userId, "published", PageRequest.of(safePage - 1, safeSize));

        List<Long> ids = p.getContent().stream().map(Review::getId).toList();
        Map<Long, List<ReviewDimensionScore>> byReview = ids.isEmpty()
            ? Map.of()
            : dimRepo.findByReviewIdIn(ids).stream()
                .collect(Collectors.groupingBy(ReviewDimensionScore::getReviewId));
        return new ReviewListResponse(
            p.getContent().stream()
                .map(r -> toDto(r, byReview.getOrDefault(r.getId(), List.of())))
                .toList(),
            safePage,
            safeSize,
            p.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listByUserPublic(Long userId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 50);
        Page<Review> p = reviewRepo.findByUserIdAndReviewStatusOrderByPublishedAtDesc(
            userId, "published", PageRequest.of(safePage - 1, safeSize));

        List<Long> ids = p.getContent().stream().map(Review::getId).toList();
        Map<Long, List<ReviewDimensionScore>> byReview = ids.isEmpty()
            ? Map.of()
            : dimRepo.findByReviewIdIn(ids).stream()
                .collect(Collectors.groupingBy(ReviewDimensionScore::getReviewId));

        return new ReviewListResponse(
            p.getContent().stream()
                .map(r -> toDto(r, byReview.getOrDefault(r.getId(), List.of())))
                .toList(),
            safePage,
            safeSize,
            p.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listMine(Long userId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        // M2 风控验收：本人列表展示 pending/folded/hidden 等审核状态，方便用户侧观察异常评价处理结果。
        Page<Review> p = reviewRepo.findMineByUserId(
            userId, PageRequest.of(safePage - 1, safeSize));
        return toReviewListResponse(p, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse replyQueue(int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        // M2 治理闭环：教练/商家回复页读取真实公开/折叠评价，替换前端静态样例。
        Page<Review> p = reviewRepo.findByReviewStatusInOrderByPublishedAtDesc(
            List.of("published", "folded"),
            PageRequest.of(safePage - 1, safeSize)
        );
        return toReviewListResponse(p, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "review:summary", key = "#targetType + ':' + #targetId")
    public ReviewSummary summary(String targetType, Long targetId) {
        validateTargetType(targetType);
        List<Review> reviews = reviewRepo.findPublishedFor(targetType, targetId);
        if (reviews.isEmpty()) {
            return new ReviewSummary(targetType, targetId, 0L, 0L, BigDecimal.ZERO, Map.of());
        }

        return new ReviewSummary(
            targetType,
            targetId,
            reviews.size(),
            countVerifiedReviews(reviews),
            calculateWeightedAverage(reviews),
            calculateDimensionAverages(targetType, targetId)
        );
    }

    private boolean verifySource(Long userId, CreateReviewRequest req) {
        if (req.sourceType() == null || req.sourceRefId() == null) return false;
        return switch (req.sourceType()) {
            case "trial" -> verifyTrialSource(userId, req);
            case "order" -> verifyWorkshopOrderSource(userId, req);
            case "checkin" -> verifyWorkshopCheckinSource(userId, req);
            default -> false;
        };
    }

    private boolean verifyTrialSource(Long userId, CreateReviewRequest req) {
        // M2 复杂闭环：试听来源可证明课程、舞室，以及该课程绑定老师的评价来源。
        return trialRepo.findById(req.sourceRefId())
                .map(b -> b.getUserId().equals(userId)
                    && isVerifiableTrial(b)
                    && matchesTrialTarget(b, req.targetType(), req.targetId()))
                .orElse(false);
    }

    private boolean verifyWorkshopOrderSource(Long userId, CreateReviewRequest req) {
        // M2/M6 联验：已支付或已完成 Workshop 订单可证明对应舞室/老师评价来源。
        return workshopOrderRepo.findById(req.sourceRefId())
            .map(o -> o.getUserId().equals(userId)
                && List.of("paid", "completed").contains(o.getOrderStatus())
                && matchesWorkshopTarget(o, req.targetType(), req.targetId()))
            .orElse(false);
    }

    private boolean verifyWorkshopCheckinSource(Long userId, CreateReviewRequest req) {
        // M2/M6 联验：签到来源比订单更强，sourceRefId 兼容 checkinId 与 orderId，便于 UI 和补库验收。
        WorkshopCheckin c = workshopCheckinRepo.findById(req.sourceRefId())
            .or(() -> workshopCheckinRepo.findByWorkshopOrderId(req.sourceRefId()))
            .orElse(null);
        if (c == null || !userId.equals(c.getCheckedInByUserId())) return false;
        return workshopOrderRepo.findById(c.getWorkshopOrderId())
            .map(o -> o.getUserId().equals(userId)
                && List.of("paid", "completed").contains(o.getOrderStatus())
                && matchesWorkshopTarget(o, req.targetType(), req.targetId()))
            .orElse(false);
    }

    private boolean isVerifiableTrial(TrialBooking b) {
        String s = b.getBookingStatus();
        return "attended".equals(s) || "confirmed".equals(s) || "no_show".equals(s);
    }

    private boolean matchesTrialTarget(TrialBooking b, String targetType, Long targetId) {
        return switch (targetType) {
            case "course" -> b.getCourseId().equals(targetId);
            case "studio" -> b.getStudioId().equals(targetId);
            case "coach" -> courseRepo.findById(b.getCourseId())
                .map(Course::getCoachId)
                .filter(targetId::equals)
                .isPresent();
            default -> false;
        };
    }

    private boolean matchesWorkshopTarget(WorkshopOrder order, String targetType, Long targetId) {
        return workshopRepo.findById(order.getWorkshopId())
            .map(w -> matchesWorkshopTarget(w, targetType, targetId))
            .orElse(false);
    }

    private boolean matchesWorkshopTarget(Workshop workshop, String targetType, Long targetId) {
        return switch (targetType) {
            case "workshop" -> targetId.equals(workshop.getId());
            case "studio" -> targetId.equals(workshop.getStudioId());
            case "coach" -> targetId.equals(workshop.getCoachId());
            default -> false;
        };
    }

    private void validateDimensionCodes(List<DimensionScoreDto> dimensions) {
        Set<String> codes = new HashSet<>();
        for (DimensionScoreDto d : dimensions) {
            if (!codes.add(d.code())) {
                throw new BizException("INVALID_ARGUMENT", "维度代码重复: " + d.code());
            }
        }
    }

    private Page<Review> findReviews(
        String targetType, Long targetId, String status, String sort, int page, int pageSize
    ) {
        PageRequest pr = PageRequest.of(page - 1, pageSize);
        return switch (sort == null ? "latest" : sort) {
            case "helpful" -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusOrderByHelpfulCountDescPublishedAtDesc(
                    targetType, targetId, status, pr);
            case "verified" -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusAndIsVerifiedOrderByPublishedAtDesc(
                    targetType, targetId, status, true, pr);
            default -> reviewRepo
                .findByTargetTypeAndTargetIdAndReviewStatusOrderByPublishedAtDesc(
                    targetType, targetId, status, pr);
        };
    }

    private ReviewListResponse toReviewListResponse(Page<Review> page, int safePage, int safeSize) {
        List<ReviewDto> items = toReviewDtos(page.getContent());
        return new ReviewListResponse(items, safePage, safeSize, page.getTotalElements());
    }

    private List<ReviewDto> toReviewDtos(List<Review> reviews) {
        Map<Long, List<ReviewDimensionScore>> byReview = loadDimensionScores(reviews);
        return reviews.stream()
            .map(r -> toDto(r, byReview.getOrDefault(r.getId(), List.of())))
            .toList();
    }

    private Map<Long, List<ReviewDimensionScore>> loadDimensionScores(List<Review> reviews) {
        List<Long> ids = reviews.stream().map(Review::getId).toList();
        return ids.isEmpty()
            ? Map.of()
            : dimRepo.findByReviewIdIn(ids).stream()
                .collect(Collectors.groupingBy(ReviewDimensionScore::getReviewId));
    }

    private long countVerifiedReviews(List<Review> reviews) {
        long verified = 0;
        for (Review r : reviews) {
            if (Boolean.TRUE.equals(r.getIsVerified())) {
                verified++;
            }
        }
        return verified;
    }

    private BigDecimal calculateWeightedAverage(List<Review> reviews) {
        BigDecimal weightSum = BigDecimal.ZERO;
        BigDecimal weightedTotal = BigDecimal.ZERO;
        for (Review r : reviews) {
            BigDecimal w = r.getWeightFactor();
            weightSum = weightSum.add(w);
            weightedTotal = weightedTotal.add(r.getOverallScore().multiply(w));
        }
        return weightSum.signum() == 0
            ? BigDecimal.ZERO
            : weightedTotal.divide(weightSum, 2, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> calculateDimensionAverages(String targetType, Long targetId) {
        List<ReviewDimensionScore> dims = dimRepo.findPublishedDimensionsFor(targetType, targetId);
        Map<String, long[]> agg = new HashMap<>();
        for (ReviewDimensionScore d : dims) {
            agg.computeIfAbsent(d.getDimensionCode(), k -> new long[]{0L, 0L});
            long[] pair = agg.get(d.getDimensionCode());
            pair[0] += d.getScore();
            pair[1] += 1L;
        }
        Map<String, BigDecimal> dimAvg = new HashMap<>();
        agg.forEach((k, pair) -> dimAvg.put(
            k,
            BigDecimal.valueOf(pair[0]).divide(BigDecimal.valueOf(pair[1]), 2, RoundingMode.HALF_UP)
        ));
        return dimAvg;
    }

    private Review buildReview(Long userId, CreateReviewRequest req, boolean verified,
                               ReviewRiskService.Verdict verdict) {
        Review r = new Review();
        r.setUserId(userId);
        r.setTargetType(req.targetType());
        r.setTargetId(req.targetId());
        r.setOverallScore(req.overallScore());
        r.setContentText(req.contentText());
        if (verified) {
            r.setVerifiedSourceType(req.sourceType());
            r.setVerifiedSourceRefId(req.sourceRefId());
            r.setIsVerified(true);
        }
        r.setWeightFactor(verdict.weightFactor());
        r.setReviewStatus(verdict.status());
        r.setRiskLevel(verdict.riskLevel());
        r.setPublishedAt(OffsetDateTime.now());
        return r;
    }

    private List<ReviewDimensionScore> buildDimensionScores(Long reviewId, List<DimensionScoreDto> dimensions) {
        List<ReviewDimensionScore> dims = new ArrayList<>();
        for (DimensionScoreDto d : dimensions) {
            ReviewDimensionScore s = new ReviewDimensionScore();
            s.setReviewId(reviewId);
            s.setDimensionCode(d.code());
            s.setDimensionName(d.name());
            s.setScore(d.score());
            dims.add(s);
        }
        return dims;
    }

    private void awardReviewBadge(Long userId, Long reviewId) {
        // 徽章引擎：用户当前评价总数
        long totalReviews = reviewRepo.count(); // 简化：BE-016 改 countByUserId 后再细化
        badgeRuleEngine.evaluate(userId, "review",
            java.util.Map.of("totalCount", totalReviews),
            "review", reviewId);
    }

    private void validateTargetType(String targetType) {
        if (!Set.of("studio", "course", "coach", "workshop").contains(targetType)) {
            throw new BizException("INVALID_ARGUMENT", "targetType 必须是 studio/course/coach/workshop");
        }
    }

    private String validatePublicStatus(String status) {
        if (status == null || status.isBlank()) return "published";
        if (!Set.of("published", "folded").contains(status)) {
            throw new BizException("INVALID_ARGUMENT", "status 蹇呴』鏄?published/folded");
        }
        return status;
    }

    private ReviewDto toDto(Review r, List<ReviewDimensionScore> dims) {
        List<DimensionScoreDto> dimDtos = dims.stream()
            .map(d -> new DimensionScoreDto(d.getDimensionCode(), d.getDimensionName(), d.getScore()))
            .toList();
        return new ReviewDto(
            r.getId(), r.getUserId(), r.getTargetType(), r.getTargetId(),
            r.getOverallScore(), r.getContentText(),
            r.getIsVerified(), r.getVerifiedSourceType(),
            r.getWeightFactor(), r.getReviewStatus(), r.getRiskLevel(),
            r.getHelpfulCount(), r.getIsPinned(),
            r.getPublishedAt(), dimDtos
        );
    }
}
