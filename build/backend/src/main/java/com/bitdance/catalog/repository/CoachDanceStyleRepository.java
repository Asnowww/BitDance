package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.CoachDanceStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachDanceStyleRepository extends JpaRepository<CoachDanceStyle, CoachDanceStyle.PK> {
    List<CoachDanceStyle> findByCoachId(Long coachId);
}
