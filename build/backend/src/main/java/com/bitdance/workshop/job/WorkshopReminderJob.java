package com.bitdance.workshop.job;

import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.domain.WorkshopSession;
import com.bitdance.workshop.repository.WorkshopRepository;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import com.bitdance.workshop.repository.WorkshopSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class WorkshopReminderJob {

    private final WorkshopOrderRepository orderRepo;
    private final WorkshopRepository workshopRepo;
    private final WorkshopSessionRepository sessionRepo;
    private final NotificationRepository notificationRepo;

    public WorkshopReminderJob(
        WorkshopOrderRepository orderRepo,
        WorkshopRepository workshopRepo,
        WorkshopSessionRepository sessionRepo,
        NotificationRepository notificationRepo
    ) {
        this.orderRepo = orderRepo;
        this.workshopRepo = workshopRepo;
        this.sessionRepo = sessionRepo;
        this.notificationRepo = notificationRepo;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    @Transactional
    public void syncUpcomingWorkshopReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        List<WorkshopOrder> orders = orderRepo.findByOrderStatusIn(List.of("paid", "checked_in"));
        for (WorkshopOrder order : orders) {
            WorkshopSession session = sessionRepo.findById(order.getWorkshopSessionId()).orElse(null);
            Workshop workshop = workshopRepo.findById(order.getWorkshopId()).orElse(null);
            if (session == null || workshop == null) continue;

            if (now.isAfter(session.getStartAt().minusHours(24)) && now.isBefore(session.getStartAt().minusHours(1))) {
                createIfAbsent(order.getUserId(), "workshop_reminder_24h", order.getId(),
                    "Workshop 明日开始", workshop.getWorkshopName() + " 将在 24 小时内开始，记得提前安排出发时间。");
            }
            if (now.isAfter(session.getStartAt().minusHours(1)) && now.isBefore(session.getEndAt())) {
                createIfAbsent(order.getUserId(), "workshop_reminder_1h", order.getId(),
                    "Workshop 即将开场", workshop.getWorkshopName() + " 已进入签到时间，打开订单页出示签到码。");
            }
            if (now.isAfter(session.getEndAt())) {
                createIfAbsent(order.getUserId(), "workshop_reminder_end", order.getId(),
                    "Workshop 已结束", "这次活动已经结束，欢迎回到订单页补充评价与复盘。");
            }
        }
    }

    private void createIfAbsent(Long userId, String noticeType, Long orderId, String title, String content) {
        if (notificationRepo.existsByUserIdAndNoticeTypeAndTargetTypeAndTargetId(
            userId, noticeType, "workshop_order", orderId
        )) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNoticeType(noticeType);
        notification.setCategory("workshop");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType("workshop_order");
        notification.setTargetId(orderId);
        notification.setIsRead(false);
        notification.setSentAt(OffsetDateTime.now());
        notificationRepo.save(notification);
    }
}
