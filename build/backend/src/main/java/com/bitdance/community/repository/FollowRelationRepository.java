package com.bitdance.community.repository;

import com.bitdance.community.domain.FollowRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRelationRepository extends JpaRepository<FollowRelation, FollowRelation.PK> {

    List<FollowRelation> findByFollowerUserId(Long followerUserId);

    List<FollowRelation> findByFolloweeUserId(Long followeeUserId);

    boolean existsByFollowerUserIdAndFolloweeUserId(Long followerUserId, Long followeeUserId);

    long countByFollowerUserId(Long followerUserId);

    long countByFolloweeUserId(Long followeeUserId);
}
