package com.bitdance.buddy.repository;

import com.bitdance.buddy.domain.PracticeRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeRatingRepository extends JpaRepository<PracticeRating, Long> {

    Optional<PracticeRating> findByPracticePostIdAndFromUserIdAndToUserId(
        Long practicePostId, Long fromUserId, Long toUserId
    );

    List<PracticeRating> findByPracticePostIdOrderByIdAsc(Long practicePostId);

    List<PracticeRating> findByPracticePostIdAndFromUserId(Long practicePostId, Long fromUserId);

    long countByPracticePostId(Long practicePostId);
}
