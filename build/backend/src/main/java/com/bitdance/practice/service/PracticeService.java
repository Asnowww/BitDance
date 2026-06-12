package com.bitdance.practice.service;

import com.bitdance.buddy.domain.PracticeRating;
import com.bitdance.buddy.repository.PracticeRatingRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.practice.domain.PracticeCompletionConfirm;
import com.bitdance.practice.domain.PracticeJoinRequest;
import com.bitdance.practice.domain.PracticePost;
import com.bitdance.practice.dto.CreatePracticeRequest;
import com.bitdance.practice.dto.JoinPracticeRequest;
import com.bitdance.practice.dto.JoinRequestDto;
import com.bitdance.practice.dto.PracticeListResponse;
import com.bitdance.practice.dto.PracticeParticipantDto;
import com.bitdance.practice.dto.PracticePostDto;
import com.bitdance.practice.repository.PracticeCompletionConfirmRepository;
import com.bitdance.practice.repository.PracticeJoinRequestRepository;
import com.bitdance.practice.repository.PracticePostRepository;
import com.bitdance.profile.domain.UserDancePreference;
import com.bitdance.profile.repository.UserDancePreferenceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    /** post 创建侧可取消的状态。 */
    private static final Set<String> CANCELABLE_POST_STATUSES =
        Set.of("published", "matched", "confirmed");

    /** 申请方可发起申请的目标 post 状态。 */
    private static final Set<String> JOINABLE_POST_STATUSES =
        Set.of("published", "matched");

    /** creator 可对申请做 accept/reject 的目标状态。 */
    private static final Set<String> ACTING_REQUEST_STATUSES = Set.of("pending");

    /** 申请方可撤回的状态。 */
    private static final Set<String> APPLICANT_CANCELABLE = Set.of("pending", "accepted");

    private final PracticePostRepository postRepo;
    private final PracticeJoinRequestRepository joinRepo;
    private final UserDancePreferenceRepository preferenceRepo;
    private final PracticeCompletionConfirmRepository completionRepo;
    private final PracticeRatingRepository ratingRepo;
    private final NotificationRepository notificationRepo;

    public PracticeService(
        PracticePostRepository postRepo,
        PracticeJoinRequestRepository joinRepo,
        UserDancePreferenceRepository preferenceRepo,
        PracticeCompletionConfirmRepository completionRepo,
        PracticeRatingRepository ratingRepo,
        NotificationRepository notificationRepo
    ) {
        this.postRepo = postRepo;
        this.joinRepo = joinRepo;
        this.preferenceRepo = preferenceRepo;
        this.completionRepo = completionRepo;
        this.ratingRepo = ratingRepo;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    @CacheEvict(cacheNames = "practice:square", allEntries = true)
    public PracticePostDto create(Long userId, CreatePracticeRequest req) {
        validatePeople(req.expectedPeopleMin(), req.expectedPeopleMax());
        validateTimes(req.startAt(), req.endAt());

        PracticePost p = new PracticePost();
        p.setCreatorUserId(userId);
        p.setDanceStyleId(req.danceStyleId());
        p.setStudioId(req.studioId());
        p.setCityId(req.cityId());
        p.setLocationName(req.locationName());
        p.setLocationAddress(req.locationAddress());
        p.setLongitude(req.longitude());
        p.setLatitude(req.latitude());
        p.setSkillLevel(req.skillLevel());
        if (req.expectedPeopleMin() != null) p.setExpectedPeopleMin(req.expectedPeopleMin());
        if (req.expectedPeopleMax() != null) p.setExpectedPeopleMax(req.expectedPeopleMax());
        p.setCurrentPeopleCount(1);
        p.setStartAt(req.startAt());
        p.setEndAt(req.endAt());
        p.setExpiresAt(req.startAt()); // schema chk: expires_at <= start_at
        p.setPostStatus("published");
        p.setDescription(req.description());
        return toDto(postRepo.save(p));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "practice:square",
        key = "(#cityId == null ? 0 : #cityId) + ':' + (#danceStyleId == null ? 0 : #danceStyleId) + ':' + (#skillLevel == null ? '' : #skillLevel) + ':' + #page + ':' + #pageSize")
    public PracticeListResponse square(
        Long cityId, Long danceStyleId, String skillLevel, int page, int pageSize
    ) {
        return square(cityId, danceStyleId, skillLevel, null, null, null, "time", page, pageSize);
    }

    @Transactional(readOnly = true)
    public PracticeListResponse square(
        Long cityId,
        Long danceStyleId,
        String skillLevel,
        BigDecimal longitude,
        BigDecimal latitude,
        String scope,
        String sort,
        int page,
        int pageSize
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<PracticePost> p = postRepo.searchSquare(cityId, danceStyleId, skillLevel, PageRequest.of(0, 200));
        List<PracticePostDto> list = new ArrayList<>(p.getContent().stream()
            .map(x -> toDto(x, distanceMeters(x, longitude, latitude)))
            .toList());
        boolean nearOnly = "nearby".equals(scope) && longitude != null && latitude != null;
        if (nearOnly) {
            list = new ArrayList<>(list.stream()
                .filter(x -> x.distanceMeters() == null || x.distanceMeters() <= 50_000L)
                .toList());
        }
        if ("distance".equals(sort) && longitude != null && latitude != null) {
            list.sort(Comparator
                .comparing((PracticePostDto x) -> x.distanceMeters() == null ? Long.MAX_VALUE : x.distanceMeters())
                .thenComparing(PracticePostDto::startAt));
        } else {
            list.sort(Comparator.comparing(PracticePostDto::startAt).thenComparing(PracticePostDto::id).reversed());
        }
        int from = Math.min((safePage - 1) * safeSize, list.size());
        int to = Math.min(from + safeSize, list.size());
        return new PracticeListResponse(
            list.subList(from, to),
            safePage, safeSize, list.size()
        );
    }

    @Transactional(readOnly = true)
    public List<PracticePostDto> recommendations(
        Long userId,
        Long cityId,
        Long danceStyleId,
        String skillLevel,
        BigDecimal longitude,
        BigDecimal latitude,
        int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<UserDancePreference> prefs = preferenceRepo.findByUserId(userId);
        Set<Long> preferredStyles = prefs.stream().map(UserDancePreference::getDanceStyleId).collect(java.util.stream.Collectors.toSet());
        String preferredLevel = prefs.stream()
            .filter(x -> x.getSkillLevel() != null && !x.getSkillLevel().isBlank())
            .findFirst().map(UserDancePreference::getSkillLevel).orElse(null);
        List<PracticePost> candidates = postRepo.recommendCandidatesFiltered(
            userId, OffsetDateTime.now(), cityId, danceStyleId, skillLevel, PageRequest.of(0, 200)
        );
        return candidates.stream()
            .map(x -> new ScoredPractice(x, distanceMeters(x, longitude, latitude),
                recommendationScore(x, preferredStyles, preferredLevel, cityId, longitude, latitude)))
            .sorted(Comparator.comparingInt(ScoredPractice::score).reversed()
                .thenComparing(x -> x.post().getStartAt()))
            .limit(safeLimit)
            .map(x -> toDto(x.post(), x.distanceMeters()))
            .toList();
    }

    @Transactional(readOnly = true)
    public PracticePostDto detail(Long id) {
        return toDto(loadPost(id));
    }

    @Transactional(readOnly = true)
    public PracticePostDto detailForUser(Long userId, Long id) {
        return toDtoForUser(loadPost(id), userId, null);
    }

    @Transactional
    @CacheEvict(cacheNames = "practice:square", allEntries = true)
    public PracticePostDto confirmCompleted(Long userId, Long postId) {
        PracticePost p = loadPost(postId);
        List<Long> participantIds = participantIdsOf(p);
        if (!participantIds.contains(userId)) {
            throw new BizException("FORBIDDEN", "Only practice participants can confirm completion");
        }
        if (!canConfirmCompletion(p)) {
            throw new BizException("PRACTICE_STATE_CONFLICT", "Practice is not ready for completion confirmation");
        }

        completionRepo.findByPracticePostIdAndUserId(postId, userId).orElseGet(() -> {
            PracticeCompletionConfirm c = new PracticeCompletionConfirm();
            c.setPracticePostId(postId);
            c.setUserId(userId);
            c.setConfirmedAt(OffsetDateTime.now());
            return completionRepo.save(c);
        });

        if (!"completed".equals(p.getPostStatus())
            && completionRepo.countByPracticePostId(postId) >= participantIds.size()) {
            p.setPostStatus("completed");
            postRepo.save(p);
        }
        return toDtoForUser(p, userId, null);
    }

    @Transactional
    @CacheEvict(cacheNames = "practice:square", allEntries = true)
    public PracticePostDto cancel(Long userId, Long postId) {
        PracticePost p = loadPost(postId);
        if (!p.getCreatorUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权取消他人约练");
        }
        if (!CANCELABLE_POST_STATUSES.contains(p.getPostStatus())) {
            throw new BizException("PRACTICE_STATE_CONFLICT",
                "当前状态 " + p.getPostStatus() + " 不可取消");
        }
        p.setPostStatus("canceled");
        return toDto(postRepo.save(p));
    }

    @Transactional
    public JoinRequestDto apply(Long userId, Long postId, JoinPracticeRequest req) {
        PracticePost p = loadPost(postId);
        if (p.getCreatorUserId().equals(userId)) {
            throw new BizException("INVALID_ARGUMENT", "不能申请加入自己的约练");
        }
        if (!JOINABLE_POST_STATUSES.contains(p.getPostStatus())) {
            throw new BizException("PRACTICE_STATE_CONFLICT",
                "当前状态 " + p.getPostStatus() + " 不接受报名");
        }
        if (p.getCurrentPeopleCount() >= p.getExpectedPeopleMax()) {
            throw new BizException("PRACTICE_FULL", "已满员");
        }
        Optional<PracticeJoinRequest> existing =
            joinRepo.findByPracticePostIdAndApplicantUserId(postId, userId);
        if (existing.isPresent() && isOpen(existing.get())) {
            throw new BizException("JOIN_DUPLICATED", "已有未结束的申请");
        }

        PracticeJoinRequest r = existing.orElseGet(PracticeJoinRequest::new);
        r.setPracticePostId(postId);
        r.setApplicantUserId(userId);
        r.setJoinStatus("pending");
        r.setJoinMessage(req == null ? null : req.message());
        r.setActedByUserId(null);
        r.setActedAt(null);
        PracticeJoinRequest saved = joinRepo.save(r);
        createNotification(
            p.getCreatorUserId(),
            "practice_join_applied",
            "practice",
            "有人申请加入约练",
            "你的约练收到一条加入申请，请及时处理。",
            "practice_post",
            p.getId()
        );
        return toJoinDto(saved);
    }

    @Transactional
    public JoinRequestDto accept(Long creatorId, Long requestId) {
        PracticeJoinRequest r = loadRequest(requestId);
        PracticePost p = loadPost(r.getPracticePostId());
        requireCreator(p, creatorId);
        requireRequestStatus(r);

        if (p.getCurrentPeopleCount() >= p.getExpectedPeopleMax()) {
            throw new BizException("PRACTICE_FULL", "已满员");
        }
        r.setJoinStatus("accepted");
        r.setActedByUserId(creatorId);
        r.setActedAt(OffsetDateTime.now());
        joinRepo.save(r);

        p.setCurrentPeopleCount(p.getCurrentPeopleCount() + 1);
        if (p.getCurrentPeopleCount() >= p.getExpectedPeopleMin()
            && "published".equals(p.getPostStatus())) {
            p.setPostStatus("matched");
        }
        if (p.getCurrentPeopleCount() >= p.getExpectedPeopleMax()) {
            p.setPostStatus("confirmed");
        }
        postRepo.save(p);
        createNotification(
            r.getApplicantUserId(),
            "practice_join_accepted",
            "practice",
            "约练申请已通过",
            "你的约练申请已通过，记得按时赴约。",
            "practice_post",
            p.getId()
        );
        return toJoinDto(r);
    }

    @Transactional
    public JoinRequestDto reject(Long creatorId, Long requestId) {
        PracticeJoinRequest r = loadRequest(requestId);
        PracticePost p = loadPost(r.getPracticePostId());
        requireCreator(p, creatorId);
        requireRequestStatus(r);

        r.setJoinStatus("rejected");
        r.setActedByUserId(creatorId);
        r.setActedAt(OffsetDateTime.now());
        PracticeJoinRequest saved = joinRepo.save(r);
        createNotification(
            r.getApplicantUserId(),
            "practice_join_rejected",
            "practice",
            "约练申请未通过",
            "你的约练申请暂未通过，可以继续寻找合适的约练。",
            "practice_post",
            p.getId()
        );
        return toJoinDto(saved);
    }

    @Transactional
    public JoinRequestDto cancelByApplicant(Long applicantId, Long requestId) {
        PracticeJoinRequest r = loadRequest(requestId);
        if (!r.getApplicantUserId().equals(applicantId)) {
            throw new BizException("FORBIDDEN", "无权撤回他人申请");
        }
        if (!APPLICANT_CANCELABLE.contains(r.getJoinStatus())) {
            throw new BizException("JOIN_STATE_CONFLICT",
                "当前状态 " + r.getJoinStatus() + " 不可撤回");
        }
        boolean wasAccepted = "accepted".equals(r.getJoinStatus());
        r.setJoinStatus("canceled");
        r.setActedByUserId(applicantId);
        r.setActedAt(OffsetDateTime.now());
        joinRepo.save(r);

        // 已接受过的撤回需要释放占位
        if (wasAccepted) {
            PracticePost p = loadPost(r.getPracticePostId());
            if (p.getCurrentPeopleCount() > 1) {
                p.setCurrentPeopleCount(p.getCurrentPeopleCount() - 1);
            }
            // 满员降级
            if ("confirmed".equals(p.getPostStatus())
                && p.getCurrentPeopleCount() < p.getExpectedPeopleMax()) {
                p.setPostStatus(
                    p.getCurrentPeopleCount() >= p.getExpectedPeopleMin() ? "matched" : "published"
                );
            } else if ("matched".equals(p.getPostStatus())
                && p.getCurrentPeopleCount() < p.getExpectedPeopleMin()) {
                p.setPostStatus("published");
            }
            postRepo.save(p);
            createNotification(
                p.getCreatorUserId(),
                "practice_join_canceled",
                "practice",
                "约练成员已退出",
                "一位已通过的成员退出了约练，请留意人数变化。",
                "practice_post",
                p.getId()
            );
        }
        return toJoinDto(r);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestDto> requestsOfPost(Long creatorId, Long postId) {
        PracticePost p = loadPost(postId);
        requireCreator(p, creatorId);
        return joinRepo.findByPracticePostIdOrderByIdDesc(postId).stream()
            .map(this::toJoinDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PracticePostDto> myPosts(Long userId) {
        return postRepo.findByCreatorUserIdOrderByIdDesc(userId).stream()
            .map(p -> toDtoForUser(p, userId, null)).toList();
    }

    @Transactional(readOnly = true)
    public List<PracticePostDto> publicPostsByCreator(Long userId) {
        return postRepo.publicPostsByCreator(userId).stream()
            .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<JoinRequestDto> myJoinRequests(Long userId) {
        return joinRepo.findByApplicantUserIdOrderByIdDesc(userId).stream()
            .map(this::toJoinDto).toList();
    }

    @Transactional
    public int closeExpired() {
        return postRepo.closeExpired(OffsetDateTime.now());
    }

    private PracticePost loadPost(Long id) {
        return postRepo.findById(id)
            .orElseThrow(() -> new BizException("PRACTICE_NOT_FOUND", "约练不存在"));
    }

    private PracticeJoinRequest loadRequest(Long id) {
        return joinRepo.findById(id)
            .orElseThrow(() -> new BizException("JOIN_REQUEST_NOT_FOUND", "申请不存在"));
    }

    private void requireCreator(PracticePost p, Long creatorId) {
        if (!p.getCreatorUserId().equals(creatorId)) {
            throw new BizException("FORBIDDEN", "无权操作他人约练的申请");
        }
    }

    private void requireRequestStatus(PracticeJoinRequest r) {
        if (!ACTING_REQUEST_STATUSES.contains(r.getJoinStatus())) {
            throw new BizException("JOIN_STATE_CONFLICT",
                "当前申请状态 " + r.getJoinStatus() + " 不可处理");
        }
    }

    private boolean isOpen(PracticeJoinRequest r) {
        return "pending".equals(r.getJoinStatus()) || "accepted".equals(r.getJoinStatus());
    }

    private void validatePeople(Integer min, Integer max) {
        int mn = min == null ? 2 : min;
        int mx = max == null ? 4 : max;
        if (mn <= 0 || mx < mn) {
            throw new BizException("INVALID_ARGUMENT", "人数下限/上限非法");
        }
    }

    private void validateTimes(OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) {
            throw new BizException("INVALID_ARGUMENT", "结束时间必须晚于开始时间");
        }
        if (start.isBefore(OffsetDateTime.now())) {
            throw new BizException("INVALID_ARGUMENT", "开始时间不能早于现在");
        }
    }

    private PracticePostDto toDto(PracticePost p) {
        return toDto(p, null);
    }

    private PracticePostDto toDto(PracticePost p, Long distanceMeters) {
        return new PracticePostDto(
            p.getId(), p.getCreatorUserId(), p.getDanceStyleId(), p.getStudioId(),
            p.getCityId(), p.getLocationName(), p.getLocationAddress(),
            p.getLongitude(), p.getLatitude(), p.getSkillLevel(),
            p.getExpectedPeopleMin(), p.getExpectedPeopleMax(), p.getCurrentPeopleCount(),
            p.getStartAt(), p.getEndAt(), p.getExpiresAt(),
            p.getPostStatus(), p.getDescription(), p.getCreatedAt(), distanceMeters
        );
    }

    private PracticePostDto toDtoForUser(PracticePost p, Long userId, Long distanceMeters) {
        List<Long> participantIds = participantIdsOf(p);
        List<PracticeCompletionConfirm> confirmations = completionRepo.findByPracticePostId(p.getId());
        Set<Long> confirmedIds = confirmations.stream()
            .map(PracticeCompletionConfirm::getUserId)
            .collect(Collectors.toSet());
        List<Long> ratedUserIds = ratingRepo.findByPracticePostIdAndFromUserId(p.getId(), userId).stream()
            .map(PracticeRating::getToUserId)
            .toList();
        Set<Long> ratedSet = new LinkedHashSet<>(ratedUserIds);
        List<PracticeParticipantDto> participants = participantIds.stream()
            .map(pid -> new PracticeParticipantDto(
                pid,
                pid.equals(p.getCreatorUserId()) ? "creator" : "participant",
                confirmedIds.contains(pid),
                ratedSet.contains(pid)
            ))
            .toList();
        boolean participatedByMe = participantIds.contains(userId);
        List<PracticeParticipantDto> ratingTargets = participatedByMe
            ? participants.stream()
                .filter(x -> !Objects.equals(x.userId(), userId))
                .toList()
            : List.of();
        boolean allConfirmed = !participantIds.isEmpty() && confirmedIds.containsAll(participantIds);
        return new PracticePostDto(
            p.getId(), p.getCreatorUserId(), p.getDanceStyleId(), p.getStudioId(),
            p.getCityId(), p.getLocationName(), p.getLocationAddress(),
            p.getLongitude(), p.getLatitude(), p.getSkillLevel(),
            p.getExpectedPeopleMin(), p.getExpectedPeopleMax(), p.getCurrentPeopleCount(),
            p.getStartAt(), p.getEndAt(), p.getExpiresAt(),
            p.getPostStatus(), p.getDescription(), p.getCreatedAt(), distanceMeters,
            participants,
            participatedByMe && confirmedIds.contains(userId),
            allConfirmed,
            ratingTargets,
            ratedUserIds
        );
    }

    private List<Long> participantIdsOf(PracticePost p) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        ids.add(p.getCreatorUserId());
        joinRepo.findByPracticePostIdOrderByIdDesc(p.getId()).stream()
            .filter(r -> "accepted".equals(r.getJoinStatus()))
            .map(PracticeJoinRequest::getApplicantUserId)
            .forEach(ids::add);
        return List.copyOf(ids);
    }

    private boolean canConfirmCompletion(PracticePost p) {
        if ("completed".equals(p.getPostStatus()) || "confirmed".equals(p.getPostStatus())) {
            return true;
        }
        return "matched".equals(p.getPostStatus())
            && p.getEndAt() != null
            && !p.getEndAt().isAfter(OffsetDateTime.now());
    }

    private void createNotification(
        Long userId,
        String noticeType,
        String category,
        String title,
        String content,
        String targetType,
        Long targetId
    ) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setNoticeType(noticeType);
        n.setCategory(category);
        n.setTitle(title);
        n.setContent(content);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setIsRead(false);
        n.setSentAt(OffsetDateTime.now());
        notificationRepo.save(n);
    }

    private int recommendationScore(
        PracticePost p,
        Set<Long> preferredStyles,
        String preferredLevel,
        Long cityId,
        BigDecimal longitude,
        BigDecimal latitude
    ) {
        int score = 20;
        if (preferredStyles.contains(p.getDanceStyleId())) score += 40;
        if (preferredLevel != null && preferredLevel.equals(p.getSkillLevel())) score += 20;
        if (cityId != null && cityId.equals(p.getCityId())) score += 15;
        Long distance = distanceMeters(p, longitude, latitude);
        if (distance != null) {
            if (distance <= 3_000) score += 20;
            else if (distance <= 10_000) score += 10;
        }
        if (p.getCurrentPeopleCount() < p.getExpectedPeopleMax()) score += 5;
        return score;
    }

    private Long distanceMeters(PracticePost p, BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null || p.getLongitude() == null || p.getLatitude() == null) {
            return null;
        }
        double lon1 = longitude.doubleValue();
        double lat1 = latitude.doubleValue();
        double lon2 = p.getLongitude().doubleValue();
        double lat2 = p.getLatitude().doubleValue();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return BigDecimal.valueOf(6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }

    private record ScoredPractice(PracticePost post, Long distanceMeters, int score) {}

    private JoinRequestDto toJoinDto(PracticeJoinRequest r) {
        return new JoinRequestDto(
            r.getId(), r.getPracticePostId(), r.getApplicantUserId(),
            r.getJoinStatus(), r.getJoinMessage(),
            r.getActedByUserId(), r.getActedAt(), r.getCreatedAt()
        );
    }
}
