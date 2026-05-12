package com.bitdance.buddy.repository;

import com.bitdance.buddy.domain.BuddyRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BuddyRelationRepository extends JpaRepository<BuddyRelation, Long> {

    Optional<BuddyRelation> findByUserIdLowAndUserIdHigh(Long userIdLow, Long userIdHigh);

    @Query("""
        select b from BuddyRelation b
        where (b.userIdLow = :userId or b.userIdHigh = :userId)
          and (:status is null or b.relationStatus = :status)
        order by b.id desc
        """)
    List<BuddyRelation> findByUser(
        @Param("userId") Long userId,
        @Param("status") String status
    );
}
