package com.bitdance.practice.repository;

import com.bitdance.practice.domain.PracticeJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeJoinRequestRepository extends JpaRepository<PracticeJoinRequest, Long> {

    List<PracticeJoinRequest> findByPracticePostIdOrderByIdDesc(Long practicePostId);

    Optional<PracticeJoinRequest> findByPracticePostIdAndApplicantUserId(
        Long practicePostId, Long applicantUserId
    );

    List<PracticeJoinRequest> findByApplicantUserIdOrderByIdDesc(Long userId);
}
