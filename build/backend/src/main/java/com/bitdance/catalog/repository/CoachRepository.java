package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
}
