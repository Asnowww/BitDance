package com.bitdance.review.repository;

import com.bitdance.review.domain.ReviewDimensionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewDimensionScoreRepository extends JpaRepository<ReviewDimensionScore, Long> {

    List<ReviewDimensionScore> findByReviewId(Long reviewId);

    List<ReviewDimensionScore> findByReviewIdIn(List<Long> reviewIds);

    @Query("""
        select s from ReviewDimensionScore s
        where s.reviewId in (
            select r.id from Review r
            where r.targetType = :targetType and r.targetId = :targetId
              and r.reviewStatus = 'published'
        )
        """)
    List<ReviewDimensionScore> findPublishedDimensionsFor(
        @Param("targetType") String targetType,
        @Param("targetId") Long targetId
    );
}
