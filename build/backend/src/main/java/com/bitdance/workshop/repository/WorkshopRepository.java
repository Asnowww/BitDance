package com.bitdance.workshop.repository;

import com.bitdance.workshop.domain.Workshop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    @Query("""
        select w from Workshop w
        where w.publishStatus = 'published'
          and (:cityId is null or w.cityId = :cityId)
          and (:danceStyleId is null or w.danceStyleId = :danceStyleId)
        order by w.id desc
        """)
    Page<Workshop> listPublished(
        @Param("cityId") Long cityId,
        @Param("danceStyleId") Long danceStyleId,
        Pageable pageable
    );

    Page<Workshop> findByAuditStatusOrderByIdAsc(String auditStatus, Pageable pageable);

    List<Workshop> findByStudioIdIn(Collection<Long> studioIds);
}
