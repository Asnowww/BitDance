package com.bitdance.community.repository;

import com.bitdance.community.domain.ReportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportTicketRepository extends JpaRepository<ReportTicket, Long> {

    boolean existsByReporterUserIdAndTargetTypeAndTargetIdAndReportStatusIn(
        Long reporterUserId, String targetType, Long targetId, java.util.List<String> statuses
    );

    @Query("""
        select t from ReportTicket t
        where (:status is null or t.reportStatus = :status)
          and (:targetType is null or t.targetType = :targetType)
        order by t.id asc
        """)
    Page<ReportTicket> search(
        @Param("status") String status,
        @Param("targetType") String targetType,
        Pageable pageable
    );
}
