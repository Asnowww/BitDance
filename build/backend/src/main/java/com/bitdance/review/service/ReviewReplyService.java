package com.bitdance.review.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewReply;
import com.bitdance.review.dto.CreateReplyRequest;
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

    public ReviewReplyService(ReviewReplyRepository replyRepo, ReviewRepository reviewRepo) {
        this.replyRepo = replyRepo;
        this.reviewRepo = reviewRepo;
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

    private ReviewReplyDto toDto(ReviewReply r) {
        return new ReviewReplyDto(
            r.getId(), r.getReviewId(), r.getReplierUserId(),
            r.getReplyContent(), r.getIsOfficial(), r.getCreatedAt()
        );
    }
}
