package com.bitdance.review.repository;

import com.bitdance.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByTargetTypeAndTargetIdAndReviewStatusOrderByPublishedAtDesc(
        String targetType, Long targetId, String status, Pageable pageable
    );

    Page<Review> findByTargetTypeAndTargetIdAndReviewStatusOrderByHelpfulCountDescPublishedAtDesc(
        String targetType, Long targetId, String status, Pageable pageable
    );

    Page<Review> findByTargetTypeAndTargetIdAndReviewStatusAndIsVerifiedOrderByPublishedAtDesc(
        String targetType, Long targetId, String status, Boolean isVerified, Pageable pageable
    );

    long countByUserIdAndTargetTypeAndTargetIdAndPublishedAtAfter(
        Long userId, String targetType, Long targetId, OffsetDateTime since
    );

    @Query("""
        select r from Review r
        where r.targetType = :targetType and r.targetId = :targetId
          and r.reviewStatus = 'published'
        """)
    List<Review> findPublishedFor(
        @Param("targetType") String targetType,
        @Param("targetId") Long targetId
    );
}
