package com.bitdance.courseorder.repository;

import com.bitdance.courseorder.domain.CourseCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseCheckinRepository extends JpaRepository<CourseCheckin, Long> {
    Optional<CourseCheckin> findByCourseOrderId(Long courseOrderId);
    Optional<CourseCheckin> findByCheckinCode(String checkinCode);
    List<CourseCheckin> findByCourseScheduleIdOrderByIdDesc(Long courseScheduleId);
}
