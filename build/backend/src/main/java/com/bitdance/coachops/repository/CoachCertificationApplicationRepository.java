package com.bitdance.coachops.repository;

import com.bitdance.coachops.domain.CoachCertificationApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoachCertificationApplicationRepository
    extends JpaRepository<CoachCertificationApplication, Long> {

    List<CoachCertificationApplication> findByUserIdOrderByIdDesc(Long userId);

    Optional<CoachCertificationApplication>
        findFirstByUserIdAndApplicationStatus(Long userId, String applicationStatus);

    Page<CoachCertificationApplication>
        findByApplicationStatusOrderByIdAsc(String applicationStatus, Pageable pageable);
}
