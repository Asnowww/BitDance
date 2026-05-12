package com.bitdance.growth.repository;

import com.bitdance.growth.domain.GrowthCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface GrowthCheckinRepository extends JpaRepository<GrowthCheckin, Long> {

    List<GrowthCheckin> findByUserIdOrderByCheckinAtDesc(Long userId);

    @Query("""
        select c from GrowthCheckin c
        where c.userId = :userId
          and c.checkinAt between :from and :to
        order by c.checkinAt asc
        """)
    List<GrowthCheckin> findByUserIdAndCheckinAtBetween(
        @Param("userId") Long userId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );
}
