package com.bitdance.profile.repository;

import com.bitdance.profile.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("""
        select p from UserProfile p
        left join PrivacySetting pr on pr.userId = p.userId
        where (:q = '' or lower(p.nickname) like lower(concat('%', :q, '%')))
          and (
            coalesce(pr.profileVisibility, 'public') = 'public'
            or p.userId = :viewerId
            or (
              :viewerId is not null
              and coalesce(pr.profileVisibility, 'public') = 'followers'
              and exists (
                select f from FollowRelation f
                where f.followerUserId = :viewerId
                  and f.followeeUserId = p.userId
              )
            )
          )
        order by p.userId asc
        """)
    Page<UserProfile> searchVisibleByNickname(
        @Param("q") String q,
        @Param("viewerId") Long viewerId,
        Pageable pageable
    );
}
