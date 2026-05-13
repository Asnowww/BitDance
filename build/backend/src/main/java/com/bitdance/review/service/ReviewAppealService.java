package com.bitdance.review.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewAppeal;
import com.bitdance.review.dto.CreateAppealRequest;
import com.bitdance.review.dto.HandleAppealRequest;
import com.bitdance.review.dto.ReviewAppealDto;
import com.bitdance.review.repository.ReviewAppealRepository;
import com.bitdance.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReviewAppealService {

    private final ReviewAppealRepository appealRepo;
    private final ReviewRepository reviewRepo;

    public ReviewAppealService(
        ReviewAppealRepository appealRepo,
        ReviewRepository reviewRepo
    ) {
        this.appealRepo = appealRepo;
        this.reviewRepo = reviewRepo;
    }

    @Transactional
    public ReviewAppealDto create(Long appellantId, CreateAppealRequest req) {
        Review r = reviewRepo.findById(req.reviewId())
            .orElseThrow(() -> new BizException("REVIEW_NOT_FOUND", "评价不存在"));
        if (!"published".equals(r.getReviewStatus()) && !"folded".equals(r.getReviewStatus())) {
            throw new BizException("REVIEW_STATE_CONFLICT", "当前评价状态不可申诉");
        }
        appealRepo.findFirstByReviewIdAndAppellantUserIdAndAppealStatus(
            req.reviewId(), appellantId, "pending"
        ).ifPresent(x -> {
            throw new BizException("APPEAL_DUPLICATED", "已有待处理的申诉");
        });
        ReviewAppeal a = new ReviewAppeal();
        a.setReviewId(req.reviewId());
        a.setAppellantUserId(appellantId);
        a.setAppealReason(req.appealReason());
        a.setEvidenceNote(req.evidenceNote());
        a.setAppealStatus("pending");
        return toDto(appealRepo.save(a));
    }

    @Transactional(readOnly = true)
    public List<ReviewAppealDto> listMine(Long appellantId) {
        return appealRepo.findByAppellantUserIdOrderByIdDesc(appellantId).stream()
            .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<ReviewAppealDto> listByStatus(String status, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        String s = status == null || status.isBlank() ? "pending" : status;
        return appealRepo.findByAppealStatusOrderByIdAsc(s, PageRequest.of(safePage - 1, safeSize))
            .map(this::toDto);
    }

    @Transactional
    public ReviewAppealDto approve(Long adminId, Long appealId, HandleAppealRequest req) {
        ReviewAppeal a = handle(adminId, appealId, "approved", req);
        // 申诉成立 → 评价隐藏
        Review r = reviewRepo.findById(a.getReviewId()).orElse(null);
        if (r != null && "published".equals(r.getReviewStatus())) {
            r.setReviewStatus("hidden");
            reviewRepo.save(r);
        }
        return toDto(a);
    }

    @Transactional
    public ReviewAppealDto reject(Long adminId, Long appealId, HandleAppealRequest req) {
        return toDto(handle(adminId, appealId, "rejected", req));
    }

    private ReviewAppeal handle(Long adminId, Long appealId, String newStatus, HandleAppealRequest req) {
        ReviewAppeal a = appealRepo.findById(appealId)
            .orElseThrow(() -> new BizException("APPEAL_NOT_FOUND", "申诉不存在"));
        if (!"pending".equals(a.getAppealStatus())) {
            throw new BizException("APPEAL_STATE_CONFLICT",
                "申诉状态 " + a.getAppealStatus() + " 不可处理");
        }
        a.setAppealStatus(newStatus);
        a.setReviewedByUserId(adminId);
        a.setReviewedAt(OffsetDateTime.now());
        if (req != null) a.setReviewRemark(req.remark());
        return appealRepo.save(a);
    }

    private ReviewAppealDto toDto(ReviewAppeal a) {
        return new ReviewAppealDto(
            a.getId(), a.getReviewId(), a.getAppellantUserId(),
            a.getAppealReason(), a.getAppealStatus(), a.getEvidenceNote(),
            a.getReviewedByUserId(), a.getReviewedAt(), a.getReviewRemark(),
            a.getCreatedAt()
        );
    }
}
