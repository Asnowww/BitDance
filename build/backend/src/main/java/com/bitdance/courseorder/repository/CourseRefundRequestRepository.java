package com.bitdance.courseorder.repository;

import com.bitdance.courseorder.domain.CourseRefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseRefundRequestRepository extends JpaRepository<CourseRefundRequest, Long> {
    Optional<CourseRefundRequest> findFirstByCourseOrderIdAndRequestStatus(Long courseOrderId, String status);
    List<CourseRefundRequest> findByRequestStatusOrderByIdAsc(String status);
    long countByRequestStatusAndCreatedAtBetween(String status, OffsetDateTime from, OffsetDateTime to);
}
