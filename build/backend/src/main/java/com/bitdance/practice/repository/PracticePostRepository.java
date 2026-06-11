package com.bitdance.practice.repository;

import com.bitdance.practice.domain.PracticePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PracticePostRepository extends JpaRepository<PracticePost, Long> {

    @Query("""
        select p from PracticePost p
        where p.postStatus in ('published','matched','confirmed')
          and (:cityId is null or p.cityId = :cityId)
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
          and (:skillLevel is null or p.skillLevel = :skillLevel)
        order by p.startAt asc, p.id desc
        """)
    Page<PracticePost> searchSquare(
        @Param("cityId") Long cityId,
        @Param("danceStyleId") Long danceStyleId,
        @Param("skillLevel") String skillLevel,
        Pageable pageable
    );

    @Query("""
        select p from PracticePost p
        where p.postStatus in ('published','matched','confirmed')
          and p.creatorUserId <> :userId
          and p.currentPeopleCount < p.expectedPeopleMax
          and p.startAt > :now
        order by p.startAt asc, p.id desc
        """)
    List<PracticePost> recommendCandidates(
        @Param("userId") Long userId,
        @Param("now") OffsetDateTime now,
        Pageable pageable
    );

    @Query("""
        select p from PracticePost p
        where p.postStatus in ('published','matched','confirmed')
          and p.creatorUserId <> :userId
          and p.currentPeopleCount < p.expectedPeopleMax
          and p.startAt > :now
          and (:cityId is null or p.cityId = :cityId)
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
          and (:skillLevel is null or p.skillLevel = :skillLevel)
        order by p.startAt asc, p.id desc
        """)
    List<PracticePost> recommendCandidatesFiltered(
        @Param("userId") Long userId,
        @Param("now") OffsetDateTime now,
        @Param("cityId") Long cityId,
        @Param("danceStyleId") Long danceStyleId,
        @Param("skillLevel") String skillLevel,
        Pageable pageable
    );

    List<PracticePost> findByCreatorUserIdOrderByIdDesc(Long userId);

    List<PracticePost> findByCreatorUserIdAndPostStatusOrderByStartAtDesc(Long userId, String postStatus);

    @Query("""
        select p from PracticePost p
        where p.postStatus = :postStatus
          and (
            p.creatorUserId = :userId
            or p.id in (
              select r.practicePostId from PracticeJoinRequest r
              where r.applicantUserId = :userId
                and r.joinStatus = 'accepted'
            )
          )
        order by p.endAt desc, p.id desc
        """)
    List<PracticePost> findByInvolvedUserAndPostStatus(
        @Param("userId") Long userId,
        @Param("postStatus") String postStatus
    );

    @Query("""
        select p from PracticePost p
        where p.creatorUserId = :creatorUserId
          and p.postStatus in ('published','matched','confirmed','completed')
        order by p.startAt desc, p.id desc
        """)
    List<PracticePost> publicPostsByCreator(@Param("creatorUserId") Long creatorUserId);

    @Modifying
    @Query("""
        update PracticePost p
        set p.postStatus = 'expired'
        where p.postStatus in ('published','matched')
          and p.expiresAt < :now
        """)
    int closeExpired(@Param("now") OffsetDateTime now);
}
