package com.bitdance.practice.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.merchant.domain.StudioClaim;
import com.bitdance.merchant.repository.StudioClaimRepository;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.practice.domain.GroupClassIntent;
import com.bitdance.practice.domain.GroupClassIntentParticipant;
import com.bitdance.practice.dto.CreateGroupClassIntentRequest;
import com.bitdance.practice.dto.GroupClassIntentDto;
import com.bitdance.practice.repository.GroupClassIntentParticipantRepository;
import com.bitdance.practice.repository.GroupClassIntentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class GroupClassIntentService {

    private static final Set<String> JOINABLE = Set.of("collecting", "matched");

    private final GroupClassIntentRepository intentRepo;
    private final GroupClassIntentParticipantRepository participantRepo;
    private final StudioClaimRepository studioClaimRepo;
    private final NotificationRepository notificationRepo;

    public GroupClassIntentService(
        GroupClassIntentRepository intentRepo,
        GroupClassIntentParticipantRepository participantRepo,
        StudioClaimRepository studioClaimRepo,
        NotificationRepository notificationRepo
    ) {
        this.intentRepo = intentRepo;
        this.participantRepo = participantRepo;
        this.studioClaimRepo = studioClaimRepo;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public GroupClassIntentDto create(Long userId, CreateGroupClassIntentRequest req) {
        GroupClassIntent g = new GroupClassIntent();
        g.setCreatorUserId(userId);
        g.setStudioId(req.studioId());
        g.setDanceStyleId(req.danceStyleId());
        g.setPreferredTimeNote(req.preferredTimeNote());
        g.setTargetPeopleCount(req.targetPeopleCount() == null ? 4 : req.targetPeopleCount());
        g.setCurrentPeopleCount(1);
        g.setIntentStatus("collecting");
        GroupClassIntent saved = intentRepo.save(g);

        GroupClassIntentParticipant p = new GroupClassIntentParticipant();
        p.setIntentId(saved.getId());
        p.setUserId(userId);
        p.setParticipantStatus("joined");
        participantRepo.save(p);
        return toDto(saved, true);
    }

    @Transactional(readOnly = true)
    public List<GroupClassIntentDto> publicList(Long currentUserId, Long studioId, Long danceStyleId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return intentRepo.publicList(studioId, danceStyleId, PageRequest.of(0, safeLimit)).stream()
            .map(x -> toDto(x, joinedBy(currentUserId, x.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupClassIntentDto> mine(Long userId) {
        return intentRepo.findMine(userId).stream()
            .map(x -> toDto(x, true))
            .toList();
    }

    @Transactional
    public GroupClassIntentDto join(Long userId, Long intentId) {
        GroupClassIntent g = load(intentId);
        if (!JOINABLE.contains(g.getIntentStatus())) {
            throw new BizException("GROUP_CLASS_STATE_CONFLICT", "当前拼课状态不可加入");
        }
        GroupClassIntentParticipant p = participantRepo.findByIntentIdAndUserId(intentId, userId)
            .orElseGet(GroupClassIntentParticipant::new);
        if ("joined".equals(p.getParticipantStatus())) {
            throw new BizException("GROUP_CLASS_JOIN_DUPLICATED", "已加入该拼课");
        }
        p.setIntentId(intentId);
        p.setUserId(userId);
        p.setParticipantStatus("joined");
        participantRepo.save(p);

        g.setCurrentPeopleCount(activeCount(intentId));
        if (g.getCurrentPeopleCount() >= g.getTargetPeopleCount()) {
            g.setIntentStatus("matched");
            notifyStudioAdmins(g);
        }
        return toDto(intentRepo.save(g), true);
    }

    @Transactional
    public GroupClassIntentDto cancel(Long userId, Long intentId) {
        GroupClassIntent g = load(intentId);
        if (g.getCreatorUserId().equals(userId)) {
            g.setIntentStatus("canceled");
            intentRepo.save(g);
            return toDto(g, false);
        }
        GroupClassIntentParticipant p = participantRepo.findByIntentIdAndUserId(intentId, userId)
            .orElseThrow(() -> new BizException("GROUP_CLASS_JOIN_NOT_FOUND", "尚未加入该拼课"));
        if (!"joined".equals(p.getParticipantStatus())) {
            throw new BizException("GROUP_CLASS_JOIN_NOT_FOUND", "尚未加入该拼课");
        }
        p.setParticipantStatus("canceled");
        participantRepo.save(p);
        g.setCurrentPeopleCount(activeCount(intentId));
        if ("matched".equals(g.getIntentStatus()) && g.getCurrentPeopleCount() < g.getTargetPeopleCount()) {
            g.setIntentStatus("collecting");
        }
        return toDto(intentRepo.save(g), false);
    }

    private GroupClassIntent load(Long id) {
        return intentRepo.findById(id)
            .orElseThrow(() -> new BizException("GROUP_CLASS_NOT_FOUND", "拼课不存在"));
    }

    private int activeCount(Long intentId) {
        return participantRepo.findByIntentIdAndParticipantStatus(intentId, "joined").size();
    }

    private boolean joinedBy(Long currentUserId, Long intentId) {
        return currentUserId != null
            && participantRepo.existsByIntentIdAndUserIdAndParticipantStatus(intentId, currentUserId, "joined");
    }

    private void notifyStudioAdmins(GroupClassIntent g) {
        for (StudioClaim claim : studioClaimRepo.findByStudioIdAndClaimStatus(g.getStudioId(), "approved")) {
            Notification n = new Notification();
            n.setUserId(claim.getApplicantUserId());
            n.setNoticeType("group_class_matched");
            n.setCategory("practice");
            n.setTitle("拼课人数已达标");
            n.setContent("有一条拼课意向已达到目标人数，请联系学员确认开课。");
            n.setTargetType("group_class_intent");
            n.setTargetId(g.getId());
            n.setSentAt(OffsetDateTime.now());
            notificationRepo.save(n);
        }
    }

    private GroupClassIntentDto toDto(GroupClassIntent g, boolean joinedByMe) {
        return new GroupClassIntentDto(
            g.getId(), g.getCreatorUserId(), g.getStudioId(), g.getDanceStyleId(),
            g.getPreferredTimeNote(), g.getTargetPeopleCount(), g.getCurrentPeopleCount(),
            g.getIntentStatus(), joinedByMe, g.getCreatedAt()
        );
    }
}
