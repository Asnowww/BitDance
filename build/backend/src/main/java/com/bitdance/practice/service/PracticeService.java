package com.bitdance.practice.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.practice.domain.PracticeJoinRequest;
import com.bitdance.practice.domain.PracticePost;
import com.bitdance.practice.dto.CreatePracticeRequest;
import com.bitdance.practice.dto.JoinPracticeRequest;
import com.bitdance.practice.dto.JoinRequestDto;
import com.bitdance.practice.dto.PracticeListResponse;
import com.bitdance.practice.dto.PracticePostDto;
import com.bitdance.practice.repository.PracticeJoinRequestRepository;
import com.bitdance.practice.repository.PracticePostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public PracticeService(
        PracticePostRepository postRepo,
        PracticeJoinRequestRepository joinRepo
    ) {
        this.postRepo = postRepo;
        this.joinRepo = joinRepo;
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
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<PracticePost> p = postRepo.searchSquare(
            cityId, danceStyleId, skillLevel,
            PageRequest.of(safePage - 1, safeSize)
        );
        return new PracticeListResponse(
            p.getContent().stream().map(this::toDto).toList(),
            safePage, safeSize, p.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public PracticePostDto detail(Long id) {
        return toDto(loadPost(id));
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
        return toJoinDto(joinRepo.save(r));
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
        return toJoinDto(joinRepo.save(r));
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
        return new PracticePostDto(
            p.getId(), p.getCreatorUserId(), p.getDanceStyleId(), p.getStudioId(),
            p.getCityId(), p.getLocationName(), p.getLocationAddress(),
            p.getLongitude(), p.getLatitude(), p.getSkillLevel(),
            p.getExpectedPeopleMin(), p.getExpectedPeopleMax(), p.getCurrentPeopleCount(),
            p.getStartAt(), p.getEndAt(), p.getExpiresAt(),
            p.getPostStatus(), p.getDescription(), p.getCreatedAt()
        );
    }

    private JoinRequestDto toJoinDto(PracticeJoinRequest r) {
        return new JoinRequestDto(
            r.getId(), r.getPracticePostId(), r.getApplicantUserId(),
            r.getJoinStatus(), r.getJoinMessage(),
            r.getActedByUserId(), r.getActedAt(), r.getCreatedAt()
        );
    }
}
