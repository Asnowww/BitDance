package com.bitdance.badge.repository;

import com.bitdance.badge.domain.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, Long> {
    List<BadgeDefinition> findByStatusOrderByIdAsc(String status);
    List<BadgeDefinition> findByStatusAndRuleType(String status, String ruleType);
}
