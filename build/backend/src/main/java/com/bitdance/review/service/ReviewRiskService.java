package com.bitdance.review.service;

import com.bitdance.iam.domain.AppUser;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * 评价权重 + 风控等级简化实现，仅 MVP 阶段。
 *
 * 输入：
 *  - 业务事实校验结果（is_verified）
 *  - 用户账号龄
 *  - 同 target 短期重复发布次数
 *
 * 输出：
 *  - weight_factor ∈ [0.1, 3.0]
 *  - risk_level（>=2 触发待审）
 *
 * 待补：相似文案检测、设备指纹、IP 集中、举报历史（接 BE-014 风控数据源）。
 */
@Service
public class ReviewRiskService {

    private static final BigDecimal MIN_WEIGHT = new BigDecimal("0.100");
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("3.000");
    private static final BigDecimal VERIFIED_BONUS = new BigDecimal("1.5");
    private static final BigDecimal YOUNG_ACCOUNT_PENALTY = new BigDecimal("0.6");
    private static final Duration YOUNG_THRESHOLD = Duration.ofDays(7);

    private final ReviewRepository reviewRepo;

    public ReviewRiskService(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public record Verdict(BigDecimal weightFactor, short riskLevel, String status) {}

    public Verdict assess(
        AppUser author,
        boolean isVerified,
        String targetType,
        Long targetId
    ) {
        BigDecimal weight = BigDecimal.ONE;
        short risk = 0;

        if (isVerified) {
            weight = weight.multiply(VERIFIED_BONUS);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (author.getCreatedAt() != null
            && Duration.between(author.getCreatedAt(), now).compareTo(YOUNG_THRESHOLD) < 0) {
            weight = weight.multiply(YOUNG_ACCOUNT_PENALTY);
            risk += 1;
        }

        OffsetDateTime since = now.minusHours(24);
        long recent = reviewRepo.countByUserIdAndTargetTypeAndTargetIdAndPublishedAtAfter(
            author.getId(), targetType, targetId, since
        );
        if (recent >= 1) {
            risk += 1;
        }

        weight = weight.max(MIN_WEIGHT).min(MAX_WEIGHT).setScale(3, RoundingMode.HALF_UP);
        String status = risk >= 2 ? "pending" : "published";
        return new Verdict(weight, risk, status);
    }
}
