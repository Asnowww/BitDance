package com.bitdance.practice.repository;

import com.bitdance.practice.domain.GroupClassIntent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupClassIntentRepository extends JpaRepository<GroupClassIntent, Long> {

    @Query("""
        select g from GroupClassIntent g
        where g.intentStatus in ('collecting','matched')
          and (:studioId is null or g.studioId = :studioId)
          and (:danceStyleId is null or g.danceStyleId = :danceStyleId)
        order by g.createdAt desc, g.id desc
        """)
    List<GroupClassIntent> publicList(
        @Param("studioId") Long studioId,
        @Param("danceStyleId") Long danceStyleId,
        Pageable pageable
    );

    @Query("""
        select distinct g from GroupClassIntent g
        left join GroupClassIntentParticipant p on p.intentId = g.id
        where g.creatorUserId = :userId or (p.userId = :userId and p.participantStatus = 'joined')
        order by g.createdAt desc, g.id desc
        """)
    List<GroupClassIntent> findMine(@Param("userId") Long userId);
}
