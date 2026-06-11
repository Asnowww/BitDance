package com.bitdance.practice.repository;

import com.bitdance.practice.domain.PracticeCompletionConfirm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeCompletionConfirmRepository extends JpaRepository<PracticeCompletionConfirm, Long> {

    Optional<PracticeCompletionConfirm> findByPracticePostIdAndUserId(Long practicePostId, Long userId);

    List<PracticeCompletionConfirm> findByPracticePostId(Long practicePostId);

    long countByPracticePostId(Long practicePostId);
}
