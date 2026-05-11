package com.bitdance.booking.repository;

import com.bitdance.booking.domain.TrialBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrialBookingRepository extends JpaRepository<TrialBooking, Long> {

    List<TrialBooking> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndCourseIdAndCourseScheduleIdAndBookingStatusIn(
        Long userId, Long courseId, Long courseScheduleId, List<String> statuses
    );

    boolean existsByUserIdAndCourseIdAndBookingStatusIn(
        Long userId, Long courseId, List<String> statuses
    );
}
