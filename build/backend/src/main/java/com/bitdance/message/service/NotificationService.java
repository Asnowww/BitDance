package com.bitdance.message.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.dto.NotificationDto;
import com.bitdance.message.dto.NotificationListResponse;
import com.bitdance.message.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public NotificationListResponse list(Long userId, String category, int page, int pageSize) {
        ensureWelcomeNotification(userId);
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        PageRequest pr = PageRequest.of(safePage - 1, safeSize);
        Page<Notification> p = (category == null || category.isBlank())
            ? repo.findByUserIdOrderByIdDesc(userId, pr)
            : repo.findByUserIdAndCategoryOrderByIdDesc(userId, category, pr);

        List<NotificationDto> items = p.getContent().stream().map(this::toDto).toList();
        long unread = repo.countByUserIdAndIsReadFalse(userId);
        return new NotificationListResponse(items, safePage, safeSize, p.getTotalElements(), unread);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        Notification n = repo.findById(id)
            .orElseThrow(() -> new BizException("NOTIFICATION_NOT_FOUND", "消息不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权访问该消息");
        }
        if (Boolean.TRUE.equals(n.getIsRead())) return;
        n.setIsRead(true);
        n.setReadAt(OffsetDateTime.now());
        repo.save(n);
    }

    @Transactional
    public int markAllRead(Long userId) {
        return repo.markAllRead(userId, OffsetDateTime.now());
    }

    private NotificationDto toDto(Notification n) {
        OffsetDateTime createdAt = n.getCreatedAt() == null ? n.getSentAt() : n.getCreatedAt();
        return new NotificationDto(
            n.getId(), n.getNoticeType(), n.getCategory(),
            n.getTitle(), n.getContent(),
            n.getTargetType(), n.getTargetId(),
            n.getIsRead(), n.getReadAt(), createdAt
        );
    }

    private void ensureWelcomeNotification(Long userId) {
        if (repo.existsByUserIdAndNoticeTypeAndTargetTypeAndTargetId(
            userId, "system_welcome", "system", userId)) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setNoticeType("system_welcome");
        n.setCategory("system");
        n.setTitle("欢迎来到丰台区舞蹈社区");
        n.setContent("新的约练、评价互动和活动提醒会及时送达。");
        n.setTargetType("system");
        n.setTargetId(userId);
        n.setIsRead(false);
        n.setSentAt(OffsetDateTime.now());
        repo.save(n);
    }
}
