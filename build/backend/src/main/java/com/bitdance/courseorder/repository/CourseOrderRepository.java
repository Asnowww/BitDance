package com.bitdance.courseorder.repository;

import com.bitdance.courseorder.domain.CourseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseOrderRepository extends JpaRepository<CourseOrder, Long> {
    Optional<CourseOrder> findFirstByUserIdAndCourseScheduleIdAndOrderStatusIn(
        Long userId, Long courseScheduleId, List<String> statuses
    );
    Optional<CourseOrder> findByCheckinCode(String checkinCode);
    List<CourseOrder> findByUserIdOrderByIdDesc(Long userId);
    List<CourseOrder> findByStudioIdOrderByIdDesc(Long studioId);
    List<CourseOrder> findByStudioIdAndOrderStatusOrderByIdDesc(Long studioId, String orderStatus);
    List<CourseOrder> findByStudioIdAndPaidAtBetween(Long studioId, OffsetDateTime from, OffsetDateTime to);
    long countByStudioIdAndCreatedAtBetween(Long studioId, OffsetDateTime from, OffsetDateTime to);
    long countByStudioIdAndOrderStatusAndCreatedAtBetween(Long studioId, String status, OffsetDateTime from, OffsetDateTime to);
}
