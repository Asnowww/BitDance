package com.bitdance.growth.repository;

import com.bitdance.growth.domain.GrowthGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GrowthGoalRepository extends JpaRepository<GrowthGoal, Long> {

    List<GrowthGoal> findByUserIdAndGoalStatusOrderByIdDesc(Long userId, String goalStatus);

    Optional<GrowthGoal> findFirstByUserIdAndGoalStatusOrderByIdDesc(Long userId, String goalStatus);

    Optional<GrowthGoal> findFirstByUserIdAndGoalStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdDesc(
        Long userId, String goalStatus, LocalDate start, LocalDate end
    );
}
