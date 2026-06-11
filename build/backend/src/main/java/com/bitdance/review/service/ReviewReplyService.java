package com.bitdance.review.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.merchant.domain.StudioCoachRelation;
import com.bitdance.merchant.repository.StudioCoachRelationRepository;
import com.bitdance.merchant.service.MerchantAccessGuard;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewReply;
import com.bitdance.review.dto.CreateReplyRequest;
import com.bitdance.review.dto.ReviewDto;
import com.bitdance.review.dto.ReviewReplyDto;
import com.bitdance.review.repository.ReviewReplyRepository;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewReplyService {

    private final ReviewReplyRepository replyRepo;
    private final ReviewRepository reviewRepo;
    private final CourseRepository courseRepo;
    private final StudioCoachRelationRepository relationRepo;
    private final MerchantAccessGuard merchantGuard;

    public ReviewReplyService(
        ReviewReplyRepository replyRepo,
        ReviewRepository reviewRepo,
        CourseRepository courseRepo,
        StudioCoachRelationRepository relationRepo,
        MerchantAccessGuard merchantGuard
    ) {
        this.replyRepo = replyRepo;
        this.reviewRepo = reviewRepo;
        this.courseRepo = courseRepo;
        this.relationRepo = relationRepo;
        this.merchantGuard = merchantGuard;
    }

    @Transactional
    public ReviewReplyDto create(Long replierId, CreateReplyRequest req) {
        Review r = reviewRepo.findById(req.reviewId())
            .orElseThrow(() -> new BizException("REVIEW_NOT_FOUND", "评价不存在"));
        if (!"published".equals(r.getReviewStatus())) {
            throw new BizException("REVIEW_STATE_CONFLICT", "评价未公开，不可回复");
        }
        ReviewReply rr = new ReviewReply();
        rr.setReviewId(r.getId());
        rr.setReplierUserId(replierId);
        rr.setReplyContent(req.replyContent());
        rr.setIsOfficial(Boolean.TRUE.equals(req.isOfficial()));
        return toDto(replyRepo.save(rr));
    }

    @Transactional
    public void delete(Long replierId, Long replyId) {
        ReviewReply rr = replyRepo.findById(replyId)
            .orElseThrow(() -> new BizException("REPLY_NOT_FOUND", "回复不存在"));
        if (!rr.getReplierUserId().equals(replierId)) {
            throw new BizException("FORBIDDEN", "无权删除他人回复");
        }
        replyRepo.delete(rr);
    }

    @Transactional(readOnly = true)
    public List<ReviewReplyDto> listByReview(Long reviewId) {
        return replyRepo.findByReviewIdOrderByIdAsc(reviewId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewReplyDto> mine(Long replierId) {
        return replyRepo.findByReplierUserIdOrderByIdDesc(replierId).stream()
            .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> pendingMerchantReviews(Long actorId, Long studioId) {
        merchantGuard.requireStudioOwnership(actorId, studioId);
        List<Long> courseIds = courseRepo.findByStudioIdOrderByIdDesc(studioId)
            .stream().map(Course::getId).toList();
        List<Long> coachIds = relationRepo.findByStudioIdOrderByIdDesc(studioId)
            .stream()
            .filter(r -> "active".equals(r.getRelationStatus()))
            .map(StudioCoachRelation::getCoachId)
            .toList();
        List<Review> reviews = new java.util.ArrayList<>();
        reviews.addAll(reviewRepo.findPublishedFor("studio", studioId));
        if (!courseIds.isEmpty()) {
            reviews.addAll(reviewRepo.findByTargetTypeAndTargetIdInAndReviewStatusOrderByPublishedAtDesc(
                "course", courseIds, "published"));
        }
        if (!coachIds.isEmpty()) {
            reviews.addAll(reviewRepo.findByTargetTypeAndTargetIdInAndReviewStatusOrderByPublishedAtDesc(
                "coach", coachIds, "published"));
        }
        return reviews.stream()
            .filter(r -> replyRepo.findByReviewIdOrderByIdAsc(r.getId()).isEmpty())
            .map(this::toReviewDto)
            .toList();
    }

    private ReviewReplyDto toDto(ReviewReply r) {
        return new ReviewReplyDto(
            r.getId(), r.getReviewId(), r.getReplierUserId(),
            r.getReplyContent(), r.getIsOfficial(), r.getCreatedAt()
        );
    }

    private ReviewDto toReviewDto(Review r) {
        return new ReviewDto(
            r.getId(), r.getUserId(), r.getTargetType(), r.getTargetId(),
            r.getOverallScore(), r.getContentText(), r.getIsVerified(),
            r.getVerifiedSourceType(), r.getWeightFactor(), r.getReviewStatus(),
            r.getRiskLevel(), r.getHelpfulCount(), r.getIsPinned(),
            r.getPublishedAt(), List.of(), List.of(), null
        );
    }
}
