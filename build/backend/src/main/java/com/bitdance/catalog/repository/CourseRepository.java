package com.bitdance.catalog.repository;

import com.bitdance.catalog.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCoachIdAndStatusOrderByIdDesc(Long coachId, String status);
}
