package com.bitdance.community.repository;

import com.bitdance.community.domain.ReportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTicketRepository extends JpaRepository<ReportTicket, Long> {

    boolean existsByReporterUserIdAndTargetTypeAndTargetIdAndReportStatusIn(
        Long reporterUserId, String targetType, Long targetId, java.util.List<String> statuses
    );
}
