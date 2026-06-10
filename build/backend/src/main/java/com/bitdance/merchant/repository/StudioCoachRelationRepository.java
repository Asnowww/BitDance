package com.bitdance.merchant.repository;

import com.bitdance.merchant.domain.StudioCoachRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudioCoachRelationRepository extends JpaRepository<StudioCoachRelation, Long> {

    List<StudioCoachRelation> findByStudioIdOrderByIdDesc(Long studioId);

    List<StudioCoachRelation> findByCoachIdAndRelationStatus(Long coachId, String relationStatus);

    List<StudioCoachRelation> findByCoachIdOrderByIdDesc(Long coachId);

    Optional<StudioCoachRelation> findFirstByStudioIdAndCoachIdAndRelationStatusIn(
        Long studioId, Long coachId, List<String> statuses
    );
}
