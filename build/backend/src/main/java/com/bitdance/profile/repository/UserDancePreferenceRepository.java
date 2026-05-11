package com.bitdance.profile.repository;

import com.bitdance.profile.domain.UserDancePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDancePreferenceRepository extends JpaRepository<UserDancePreference, Long> {
    List<UserDancePreference> findByUserId(Long userId);

    @Modifying
    @Query("delete from UserDancePreference p where p.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
