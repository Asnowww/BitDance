package com.bitdance.review.repository;

import com.bitdance.review.domain.ReviewAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewAppealRepository extends JpaRepository<ReviewAppeal, Long> {
    List<ReviewAppeal> findByAppellantUserIdOrderByIdDesc(Long appellantUserId);
    List<ReviewAppeal> findByReviewIdInOrderByIdDesc(Collection<Long> reviewIds);
    Optional<ReviewAppeal> findFirstByReviewIdAndAppellantUserIdAndAppealStatus(
        Long reviewId, Long appellantUserId, String appealStatus
    );
    Page<ReviewAppeal> findByAppealStatusOrderByIdAsc(String appealStatus, Pageable pageable);
}
