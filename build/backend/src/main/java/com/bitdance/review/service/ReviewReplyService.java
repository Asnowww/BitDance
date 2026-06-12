package com.bitdance.review.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewReply;
import com.bitdance.review.dto.CreateReplyRequest;
import com.bitdance.review.dto.ReviewReplyDto;
import com.bitdance.review.repository.ReviewReplyRepository;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReviewReplyService {

    private final ReviewReplyRepository replyRepo;
    private final ReviewRepository reviewRepo;
    private final NotificationRepository notificationRepo;

    public ReviewReplyService(
        ReviewReplyRepository replyRepo,
        ReviewRepository reviewRepo,
        NotificationRepository notificationRepo
    ) {
        this.replyRepo = replyRepo;
        this.reviewRepo = reviewRepo;
        this.notificationRepo = notificationRepo;
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
        ReviewReply saved = replyRepo.save(rr);
        notifyReviewAuthor(r, replierId);
        return toDto(saved);
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

    private ReviewReplyDto toDto(ReviewReply r) {
        return new ReviewReplyDto(
            r.getId(), r.getReviewId(), r.getReplierUserId(),
            r.getReplyContent(), r.getIsOfficial(), r.getCreatedAt()
        );
    }

    private void notifyReviewAuthor(Review review, Long replierId) {
        if (Objects.equals(review.getUserId(), replierId)) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(review.getUserId());
        n.setNoticeType("review_replied");
        n.setCategory("review");
        n.setTitle("你的评价收到回复");
        n.setContent("有人回复了你的评价，点击查看互动详情。");
        n.setTargetType("review");
        n.setTargetId(review.getId());
        n.setIsRead(false);
        n.setSentAt(OffsetDateTime.now());
        notificationRepo.save(n);
    }
}
