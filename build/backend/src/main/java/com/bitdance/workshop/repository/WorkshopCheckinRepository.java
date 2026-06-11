package com.bitdance.workshop.repository;

import com.bitdance.workshop.domain.WorkshopCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkshopCheckinRepository extends JpaRepository<WorkshopCheckin, Long> {
    Optional<WorkshopCheckin> findByWorkshopOrderId(Long workshopOrderId);

    List<WorkshopCheckin> findByWorkshopOrderIdIn(List<Long> workshopOrderIds);
}
