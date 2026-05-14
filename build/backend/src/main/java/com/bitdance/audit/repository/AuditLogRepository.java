package com.bitdance.audit.repository;

import com.bitdance.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        select a from AuditLog a
        where (:actor is null or a.actorUserId = :actor)
          and (:action is null or a.actionCode = :action)
          and (:targetType is null or a.targetType = :targetType)
        order by a.id desc
        """)
    Page<AuditLog> search(
        @Param("actor") Long actor,
        @Param("action") String action,
        @Param("targetType") String targetType,
        Pageable pageable
    );
}
