package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.StudioDanceStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudioDanceStyleRepository extends JpaRepository<StudioDanceStyle, StudioDanceStyle.PK> {
    List<StudioDanceStyle> findByStudioId(Long studioId);
}
