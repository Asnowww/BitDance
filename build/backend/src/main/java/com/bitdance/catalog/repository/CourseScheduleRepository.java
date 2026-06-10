package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    List<CourseSchedule> findByCourseIdAndStartAtBetweenOrderByStartAtAsc(
        Long courseId, OffsetDateTime from, OffsetDateTime to
    );

    List<CourseSchedule> findByStudioIdAndStartAtBetweenOrderByStartAtAsc(
        Long studioId, OffsetDateTime from, OffsetDateTime to
    );

    @Modifying
    @Query("""
        update CourseSchedule s
        set s.bookedCount = s.bookedCount + 1
        where s.id = :scheduleId
          and (s.capacity is null or s.bookedCount < s.capacity)
        """)
    int tryReserveSeat(@Param("scheduleId") Long scheduleId);

    @Modifying
    @Query("""
        update CourseSchedule s
        set s.bookedCount = s.bookedCount - 1
        where s.id = :scheduleId and s.bookedCount > 0
        """)
    int releaseSeat(@Param("scheduleId") Long scheduleId);
}
