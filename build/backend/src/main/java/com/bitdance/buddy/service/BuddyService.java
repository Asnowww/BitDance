package com.bitdance.buddy.service;

import com.bitdance.buddy.domain.BuddyRelation;
import com.bitdance.buddy.domain.PracticeRating;
import com.bitdance.buddy.dto.BuddyDto;
import com.bitdance.buddy.dto.CreateRatingRequest;
import com.bitdance.buddy.dto.RatingDto;
import com.bitdance.buddy.repository.BuddyRelationRepository;
import com.bitdance.buddy.repository.PracticeRatingRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.practice.domain.PracticeJoinRequest;
import com.bitdance.practice.domain.PracticePost;
import com.bitdance.practice.repository.PracticeJoinRequestRepository;
import com.bitdance.practice.repository.PracticePostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BuddyService {

    /** post 可发起评价的状态。 */
    private static final Set<String> RATABLE_POST_STATUSES = Set.of("confirmed", "completed");

    private final PracticeRatingRepository ratingRepo;
    private final BuddyRelationRepository buddyRepo;
    private final PracticePostRepository postRepo;
    private final PracticeJoinRequestRepository joinRepo;

    public BuddyService(
        PracticeRatingRepository ratingRepo,
        BuddyRelationRepository buddyRepo,
        PracticePostRepository postRepo,
        PracticeJoinRequestRepository joinRepo
    ) {
        this.ratingRepo = ratingRepo;
        this.buddyRepo = buddyRepo;
        this.postRepo = postRepo;
        this.joinRepo = joinRepo;
    }

    // ============ Rating ============

    @Transactional
    public RatingDto rate(Long fromUserId, Long practicePostId, CreateRatingRequest req) {
        if (fromUserId.equals(req.toUserId())) {
            throw new BizException("INVALID_ARGUMENT", "不能评价自己");
        }
        PracticePost post = postRepo.findById(practicePostId)
            .orElseThrow(() -> new BizException("PRACTICE_NOT_FOUND", "约练不存在"));
        if (!RATABLE_POST_STATUSES.contains(post.getPostStatus())) {
            throw new BizException("PRACTICE_STATE_CONFLICT",
                "当前状态 " + post.getPostStatus() + " 不可评价");
        }
        Set<Long> participants = participantsOf(post);
        if (!participants.contains(fromUserId)) {
            throw new BizException("FORBIDDEN", "你不是该约练的参与者");
        }
        if (!participants.contains(req.toUserId())) {
            throw new BizException("INVALID_ARGUMENT", "评价对象不是该约练的参与者");
        }
        ratingRepo.findByPracticePostIdAndFromUserIdAndToUserId(practicePostId, fromUserId, req.toUserId())
            .ifPresent(r -> {
                throw new BizException("RATING_DUPLICATED", "已对该参与者评价过");
            });

        PracticeRating r = new PracticeRating();
        r.setPracticePostId(practicePostId);
        r.setFromUserId(fromUserId);
        r.setToUserId(req.toUserId());
        r.setPunctualityScore(req.punctuality());
        r.setFriendlinessScore(req.friendliness());
        r.setSkillMatchScore(req.skillMatch());
        r.setRatingComment(req.comment());
        PracticeRating saved = ratingRepo.save(r);

        // 反方向也已评价 → 建立搭子关系
        boolean reverseExists = ratingRepo.findByPracticePostIdAndFromUserIdAndToUserId(
            practicePostId, req.toUserId(), fromUserId
        ).isPresent();
        if (reverseExists) {
            ensureBuddy(fromUserId, req.toUserId(), practicePostId);
            // 全员两两都评完则把 post 转 completed
            int expectedRatingPairs = participants.size() * (participants.size() - 1);
            if (ratingRepo.countByPracticePostId(practicePostId) >= expectedRatingPairs
                && !"completed".equals(post.getPostStatus())) {
                post.setPostStatus("completed");
                postRepo.save(post);
            }
        }
        return toRatingDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RatingDto> ratingsOfPost(Long currentUserId, Long practicePostId) {
        PracticePost post = postRepo.findById(practicePostId)
            .orElseThrow(() -> new BizException("PRACTICE_NOT_FOUND", "约练不存在"));
        if (!participantsOf(post).contains(currentUserId)) {
            throw new BizException("FORBIDDEN", "你不是该约练的参与者");
        }
        return ratingRepo.findByPracticePostIdOrderByIdAsc(practicePostId).stream()
            .map(this::toRatingDto).toList();
    }

    // ============ Buddy ============

    @Transactional(readOnly = true)
    public List<BuddyDto> listMyBuddies(Long userId, String status) {
        return buddyRepo.findByUser(userId, status).stream()
            .map(b -> {
                long peer = b.getUserIdLow().equals(userId) ? b.getUserIdHigh() : b.getUserIdLow();
                return new BuddyDto(b.getId(), peer,
                    b.getSourcePracticePostId(), b.getRelationStatus(), b.getCreatedAt());
            })
            .toList();
    }

    @Transactional
    public BuddyDto block(Long userId, Long peerUserId) {
        BuddyRelation b = loadRelation(userId, peerUserId);
        b.setRelationStatus("blocked");
        buddyRepo.save(b);
        return toBuddyDto(b, userId);
    }

    @Transactional
    public BuddyDto removeBuddy(Long userId, Long peerUserId) {
        BuddyRelation b = loadRelation(userId, peerUserId);
        b.setRelationStatus("inactive");
        buddyRepo.save(b);
        return toBuddyDto(b, userId);
    }

    // ============ Helpers ============

    private Set<Long> participantsOf(PracticePost post) {
        Set<Long> ids = new HashSet<>();
        ids.add(post.getCreatorUserId());
        for (PracticeJoinRequest r : joinRepo.findByPracticePostIdOrderByIdDesc(post.getId())) {
            if ("accepted".equals(r.getJoinStatus())) {
                ids.add(r.getApplicantUserId());
            }
        }
        return ids;
    }

    private void ensureBuddy(Long a, Long b, Long sourcePostId) {
        long low = Math.min(a, b);
        long high = Math.max(a, b);
        if (buddyRepo.findByUserIdLowAndUserIdHigh(low, high).isPresent()) return;
        BuddyRelation rel = new BuddyRelation();
        rel.setUserIdLow(low);
        rel.setUserIdHigh(high);
        rel.setSourcePracticePostId(sourcePostId);
        rel.setRelationStatus("active");
        buddyRepo.save(rel);
    }

    private BuddyRelation loadRelation(Long userId, Long peerUserId) {
        if (userId.equals(peerUserId)) {
            throw new BizException("INVALID_ARGUMENT", "对方用户 id 非法");
        }
        long low = Math.min(userId, peerUserId);
        long high = Math.max(userId, peerUserId);
        return buddyRepo.findByUserIdLowAndUserIdHigh(low, high)
            .orElseThrow(() -> new BizException("BUDDY_NOT_FOUND", "搭子关系不存在"));
    }

    private RatingDto toRatingDto(PracticeRating r) {
        return new RatingDto(
            r.getId(), r.getPracticePostId(), r.getFromUserId(), r.getToUserId(),
            r.getPunctualityScore(), r.getFriendlinessScore(), r.getSkillMatchScore(),
            r.getRatingComment(), r.getCreatedAt()
        );
    }

    private BuddyDto toBuddyDto(BuddyRelation b, Long currentUserId) {
        long peer = b.getUserIdLow().equals(currentUserId) ? b.getUserIdHigh() : b.getUserIdLow();
        return new BuddyDto(b.getId(), peer,
            b.getSourcePracticePostId(), b.getRelationStatus(), b.getCreatedAt());
    }
}
