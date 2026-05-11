package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    List<CourseSchedule> findByCourseIdAndStartAtBetweenOrderByStartAtAsc(
        Long courseId, OffsetDateTime from, OffsetDateTime to
    );

    List<CourseSchedule> findByStudioIdAndStartAtBetweenOrderByStartAtAsc(
        Long studioId, OffsetDateTime from, OffsetDateTime to
    );
}
