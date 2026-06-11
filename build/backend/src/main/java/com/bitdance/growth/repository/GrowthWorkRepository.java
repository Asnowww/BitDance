package com.bitdance.growth.repository;

import com.bitdance.growth.domain.GrowthWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface GrowthWorkRepository extends JpaRepository<GrowthWork, Long> {
    List<GrowthWork> findByUserIdOrderByIdDesc(Long userId);
    List<GrowthWork> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId, OffsetDateTime from, OffsetDateTime to
    );
}
