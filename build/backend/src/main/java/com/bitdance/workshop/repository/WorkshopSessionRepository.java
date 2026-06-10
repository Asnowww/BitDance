package com.bitdance.workshop.repository;

import com.bitdance.workshop.domain.WorkshopSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkshopSessionRepository extends JpaRepository<WorkshopSession, Long> {

    List<WorkshopSession> findByWorkshopIdOrderByStartAtAsc(Long workshopId);

    @Query("""
        select s.workshopId, coalesce(sum(s.capacity), 0), coalesce(sum(s.soldCount), 0)
        from WorkshopSession s
        where s.workshopId in :workshopIds
        group by s.workshopId
        """)
    List<Object[]> statsByWorkshopIds(@Param("workshopIds") List<Long> workshopIds);

    /**
     * 原子占座：仅在未满员时才 +1，返回受影响行数。
     * 0 表示满员或不存在，调用方据此抛 WORKSHOP_FULL。
     */
    @Modifying
    @Query("""
        update WorkshopSession s
        set s.soldCount = s.soldCount + 1
        where s.id = :sessionId and s.soldCount < s.capacity
        """)
    int tryReserveSeat(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("""
        update WorkshopSession s
        set s.soldCount = s.soldCount - 1
        where s.id = :sessionId and s.soldCount > 0
        """)
    int releaseSeat(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("""
        update WorkshopSession s
        set s.checkinCount = s.checkinCount + 1
        where s.id = :sessionId and s.checkinCount < s.soldCount
        """)
    int incrementCheckin(@Param("sessionId") Long sessionId);
}
