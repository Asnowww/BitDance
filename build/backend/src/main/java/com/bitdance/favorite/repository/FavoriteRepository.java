package com.bitdance.favorite.repository;

import com.bitdance.favorite.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);

    List<Favorite> findByUserIdAndTargetTypeOrderByIdDesc(Long userId, String targetType);

    @Query("select f.targetId from Favorite f " +
        "where f.userId = :userId and f.targetType = :targetType and f.targetId in :ids")
    List<Long> findFavoredIds(
        @Param("userId") Long userId,
        @Param("targetType") String targetType,
        @Param("ids") List<Long> ids
    );

    @Query("""
        select f.targetId as targetId, count(f.id) as cnt
        from Favorite f
        where f.targetType = :targetType and f.targetId in :ids
        group by f.targetId
        """)
    List<java.util.Map<String, Object>> countGroupedByTargetIds(
        @Param("targetType") String targetType,
        @Param("ids") List<Long> ids
    );
}
