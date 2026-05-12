package com.bitdance.growth.repository;

import com.bitdance.growth.domain.GrowthBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrowthBadgeRepository extends JpaRepository<GrowthBadge, Long> {
    List<GrowthBadge> findByUserIdOrderByAwardedAtDesc(Long userId);
}
