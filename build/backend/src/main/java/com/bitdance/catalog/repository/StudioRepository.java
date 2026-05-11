package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepository extends JpaRepository<Studio, Long> {
}
