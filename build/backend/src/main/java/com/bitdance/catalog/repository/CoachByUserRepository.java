package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 提供 coach 表按 user_id 查询的入口（CoachRepository 主要负责按 id 查）。
 * 拆成独立接口避免修改 BE-006 的既有 CoachRepository。
 */
public interface CoachByUserRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
