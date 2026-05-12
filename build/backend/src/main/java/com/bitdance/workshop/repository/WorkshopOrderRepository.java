package com.bitdance.workshop.repository;

import com.bitdance.workshop.domain.WorkshopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkshopOrderRepository extends JpaRepository<WorkshopOrder, Long> {

    Optional<WorkshopOrder> findByOrderNo(String orderNo);

    /** 用于幂等：同一用户同一场次只能有一笔 pending_payment / paid 单。 */
    Optional<WorkshopOrder> findFirstByUserIdAndWorkshopSessionIdAndOrderStatusIn(
        Long userId, Long workshopSessionId, List<String> statuses
    );

    List<WorkshopOrder> findByUserIdOrderByIdDesc(Long userId);
}
