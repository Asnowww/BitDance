package com.bitdance.review.repository;

import com.bitdance.review.domain.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    List<ReviewReply> findByReviewIdOrderByIdAsc(Long reviewId);
    List<ReviewReply> findByReplierUserIdOrderByIdDesc(Long replierUserId);
}
