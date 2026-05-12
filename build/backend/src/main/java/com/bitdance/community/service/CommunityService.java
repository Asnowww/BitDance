package com.bitdance.community.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.community.domain.ContentComment;
import com.bitdance.community.domain.ContentLike;
import com.bitdance.community.domain.ContentPost;
import com.bitdance.community.domain.ContentPostTopic;
import com.bitdance.community.domain.FollowRelation;
import com.bitdance.community.domain.ReportTicket;
import com.bitdance.community.domain.TopicTag;
import com.bitdance.community.dto.CommentDto;
import com.bitdance.community.dto.CreateCommentRequest;
import com.bitdance.community.dto.CreatePostRequest;
import com.bitdance.community.dto.PostDto;
import com.bitdance.community.dto.PostListResponse;
import com.bitdance.community.dto.ReportRequest;
import com.bitdance.community.dto.TopicDto;
import com.bitdance.community.repository.ContentCommentRepository;
import com.bitdance.community.repository.ContentLikeRepository;
import com.bitdance.community.repository.ContentPostRepository;
import com.bitdance.community.repository.ContentPostTopicRepository;
import com.bitdance.community.repository.FollowRelationRepository;
import com.bitdance.community.repository.ReportTicketRepository;
import com.bitdance.community.repository.TopicTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommunityService {

    private static final Set<String> ALLOWED_REPORT_TARGET_TYPES = Set.of(
        "content_post", "content_comment"
    );

    private final ContentPostRepository postRepo;
    private final ContentCommentRepository commentRepo;
    private final ContentLikeRepository likeRepo;
    private final TopicTagRepository topicRepo;
    private final ContentPostTopicRepository postTopicRepo;
    private final FollowRelationRepository followRepo;
    private final ReportTicketRepository reportRepo;

    public CommunityService(
        ContentPostRepository postRepo,
        ContentCommentRepository commentRepo,
        ContentLikeRepository likeRepo,
        TopicTagRepository topicRepo,
        ContentPostTopicRepository postTopicRepo,
        FollowRelationRepository followRepo,
        ReportTicketRepository reportRepo
    ) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.likeRepo = likeRepo;
        this.topicRepo = topicRepo;
        this.postTopicRepo = postTopicRepo;
        this.followRepo = followRepo;
        this.reportRepo = reportRepo;
    }

    // ============ Post ============

    @Transactional
    public PostDto createPost(Long userId, CreatePostRequest req) {
        ContentPost p = new ContentPost();
        p.setAuthorUserId(userId);
        p.setPostType(req.postType() == null ? "note" : req.postType());
        p.setContentText(req.contentText());
        p.setDanceStyleId(req.danceStyleId());
        p.setRelatedCourseId(req.relatedCourseId());
        p.setRelatedWorkshopId(req.relatedWorkshopId());
        p.setCityId(req.cityId());
        p.setLocationName(req.locationName());
        p.setLongitude(req.longitude());
        p.setLatitude(req.latitude());
        p.setVisibility(req.visibility() == null ? "public" : req.visibility());
        p.setPostStatus("published");
        p.setPublishedAt(OffsetDateTime.now());
        ContentPost saved = postRepo.save(p);

        if (req.topicNames() != null && !req.topicNames().isEmpty()) {
            for (String name : new HashSet<>(req.topicNames())) {
                if (name == null || name.isBlank()) continue;
                TopicTag tag = topicRepo.findByTopicName(name.trim()).orElseGet(() -> {
                    TopicTag t = new TopicTag();
                    t.setTopicCode("u-" + System.currentTimeMillis() + "-" + Math.abs(name.hashCode() % 10000));
                    t.setTopicName(name.trim());
                    t.setCreatorUserId(userId);
                    t.setIsSystem(false);
                    t.setStatus("active");
                    return topicRepo.save(t);
                });
                ContentPostTopic link = new ContentPostTopic();
                link.setContentPostId(saved.getId());
                link.setTopicTagId(tag.getId());
                postTopicRepo.save(link);
            }
        }
        return toPostDto(saved, userId);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!p.getAuthorUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人动态");
        }
        if ("deleted".equals(p.getPostStatus())) return;
        p.setPostStatus("deleted");
        postRepo.save(p);
    }

    @Transactional(readOnly = true)
    public PostDto detail(Long postId, Long currentUserId) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if ("deleted".equals(p.getPostStatus()) || "hidden".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        if ("private".equals(p.getVisibility())
            && (currentUserId == null || !currentUserId.equals(p.getAuthorUserId()))) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        if ("followers".equals(p.getVisibility())
            && currentUserId != null
            && !currentUserId.equals(p.getAuthorUserId())
            && !followRepo.existsByFollowerUserIdAndFolloweeUserId(currentUserId, p.getAuthorUserId())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        return toPostDto(p, currentUserId);
    }

    @Transactional(readOnly = true)
    public PostListResponse feed(
        String scope, Long danceStyleId, Long topicId, int page, int pageSize, Long currentUserId
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        PageRequest pr = PageRequest.of(safePage - 1, safeSize);
        Page<ContentPost> p;
        if (topicId != null) {
            p = postRepo.byTopic(topicId, pr);
        } else if ("follow".equals(scope)) {
            if (currentUserId == null) {
                return new PostListResponse(List.of(), safePage, safeSize, 0L);
            }
            List<Long> followees = followRepo.findByFollowerUserId(currentUserId)
                .stream().map(FollowRelation::getFolloweeUserId).toList();
            if (followees.isEmpty()) {
                return new PostListResponse(List.of(), safePage, safeSize, 0L);
            }
            p = postRepo.followingFeed(followees, danceStyleId, pr);
        } else {
            p = postRepo.recommend(danceStyleId, pr);
        }
        List<PostDto> items = enrichBatch(p.getContent(), currentUserId);
        return new PostListResponse(items, safePage, safeSize, p.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostListResponse search(String q, int page, int pageSize, Long currentUserId) {
        if (q == null || q.isBlank()) {
            return new PostListResponse(List.of(), Math.max(1, page),
                Math.min(Math.max(1, pageSize), 100), 0L);
        }
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<ContentPost> p = postRepo.search(q.trim(), PageRequest.of(safePage - 1, safeSize));
        return new PostListResponse(enrichBatch(p.getContent(), currentUserId),
            safePage, safeSize, p.getTotalElements());
    }

    // ============ Like ============

    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!"published".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        ContentLike.PK pk = new ContentLike.PK(postId, userId);
        boolean liked;
        if (likeRepo.existsById(pk)) {
            likeRepo.deleteById(pk);
            liked = false;
        } else {
            ContentLike like = new ContentLike();
            like.setContentPostId(postId);
            like.setUserId(userId);
            likeRepo.save(like);
            liked = true;
        }
        long count = likeRepo.countByContentPostId(postId);
        return Map.of("liked", liked, "likeCount", count);
    }

    // ============ Comment ============

    @Transactional
    public CommentDto createComment(Long userId, Long postId, CreateCommentRequest req) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!"published".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        ContentComment c = new ContentComment();
        c.setContentPostId(postId);
        c.setUserId(userId);
        c.setParentCommentId(req.parentCommentId());
        c.setReplyToUserId(req.replyToUserId());
        c.setCommentText(req.commentText());
        c.setCommentStatus("published");
        return toCommentDto(commentRepo.save(c));
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long postId) {
        return commentRepo.findByContentPostIdAndCommentStatusOrderByIdAsc(postId, "published")
            .stream().map(this::toCommentDto).toList();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        ContentComment c = commentRepo.findById(commentId)
            .orElseThrow(() -> new BizException("COMMENT_NOT_FOUND", "评论不存在"));
        if (!c.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权删除他人评论");
        }
        c.setCommentStatus("deleted");
        commentRepo.save(c);
    }

    // ============ Topic ============

    @Transactional(readOnly = true)
    public List<TopicDto> listTopics() {
        return topicRepo.findByStatusOrderByIdDesc("active").stream()
            .map(t -> {
                long count = postTopicRepo.countByTopicId(t.getId());
                return new TopicDto(t.getId(), t.getTopicCode(), t.getTopicName(), count, count >= 4);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public PostListResponse postsByTopicName(
        String topicName, int page, int pageSize, Long currentUserId
    ) {
        TopicTag t = topicRepo.findByTopicName(topicName)
            .orElseThrow(() -> new BizException("TOPIC_NOT_FOUND", "话题不存在"));
        return feed(null, null, t.getId(), page, pageSize, currentUserId);
    }

    // ============ Follow ============

    @Transactional
    public Map<String, Object> toggleFollow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BizException("INVALID_ARGUMENT", "不能关注自己");
        }
        FollowRelation.PK pk = new FollowRelation.PK(followerId, followeeId);
        boolean following;
        if (followRepo.existsById(pk)) {
            followRepo.deleteById(pk);
            following = false;
        } else {
            FollowRelation r = new FollowRelation();
            r.setFollowerUserId(followerId);
            r.setFolloweeUserId(followeeId);
            followRepo.save(r);
            following = true;
        }
        return Map.of(
            "following", following,
            "followerCount", followRepo.countByFolloweeUserId(followeeId)
        );
    }

    @Transactional(readOnly = true)
    public List<Long> listFollowees(Long userId) {
        return followRepo.findByFollowerUserId(userId)
            .stream().map(FollowRelation::getFolloweeUserId).toList();
    }

    // ============ Report ============

    @Transactional
    public Map<String, Object> reportPost(Long userId, Long postId, ReportRequest req) {
        if (!postRepo.existsById(postId)) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        return doReport(userId, "content_post", postId, req);
    }

    @Transactional
    public Map<String, Object> reportComment(Long userId, Long commentId, ReportRequest req) {
        if (!commentRepo.existsById(commentId)) {
            throw new BizException("COMMENT_NOT_FOUND", "评论不存在");
        }
        return doReport(userId, "content_comment", commentId, req);
    }

    private Map<String, Object> doReport(
        Long userId, String targetType, Long targetId, ReportRequest req
    ) {
        if (!ALLOWED_REPORT_TARGET_TYPES.contains(targetType)) {
            throw new BizException("INVALID_ARGUMENT", "举报对象类型非法");
        }
        if (reportRepo.existsByReporterUserIdAndTargetTypeAndTargetIdAndReportStatusIn(
            userId, targetType, targetId, List.of("pending", "processing"))) {
            throw new BizException("REPORT_DUPLICATED", "已存在未处理的举报");
        }
        ReportTicket t = new ReportTicket();
        t.setReporterUserId(userId);
        t.setTargetType(targetType);
        t.setTargetId(targetId);
        t.setReasonCode(req.reasonCode());
        t.setReasonDetail(req.reasonDetail());
        t.setReportStatus("pending");
        ReportTicket saved = reportRepo.save(t);
        return Map.of("reported", true, "ticketId", saved.getId());
    }

    // ============ Helpers ============

    private List<PostDto> enrichBatch(List<ContentPost> posts, Long currentUserId) {
        if (posts.isEmpty()) return List.of();
        List<Long> ids = posts.stream().map(ContentPost::getId).toList();

        Map<Long, Long> likeCounts = new HashMap<>();
        for (Map<String, Object> row : likeRepo.countGroupedByPostIds(ids)) {
            likeCounts.put(((Number) row.get("postId")).longValue(),
                ((Number) row.get("cnt")).longValue());
        }
        Map<Long, Long> commentCounts = new HashMap<>();
        for (Map<String, Object> row : commentRepo.countGroupedByPostIds(ids)) {
            commentCounts.put(((Number) row.get("postId")).longValue(),
                ((Number) row.get("cnt")).longValue());
        }
        Set<Long> likedSet = currentUserId == null
            ? Set.of()
            : new HashSet<>(likeRepo.findLikedPostIds(currentUserId, ids));

        Map<Long, List<Long>> postIdToTopicIds = new HashMap<>();
        for (Map<String, Object> row : postTopicRepo.findByPostIds(ids)) {
            Long postId = ((Number) row.get("postId")).longValue();
            Long topicId = ((Number) row.get("topicId")).longValue();
            postIdToTopicIds.computeIfAbsent(postId, k -> new ArrayList<>()).add(topicId);
        }
        Set<Long> allTopicIds = new HashSet<>();
        postIdToTopicIds.values().forEach(allTopicIds::addAll);
        Map<Long, TopicTag> topicMap = new HashMap<>();
        if (!allTopicIds.isEmpty()) {
            for (TopicTag t : topicRepo.findByIdIn(new ArrayList<>(allTopicIds))) {
                topicMap.put(t.getId(), t);
            }
        }

        List<PostDto> out = new ArrayList<>(posts.size());
        for (ContentPost p : posts) {
            long lc = likeCounts.getOrDefault(p.getId(), 0L);
            long cc = commentCounts.getOrDefault(p.getId(), 0L);
            List<TopicDto> topics = postIdToTopicIds
                .getOrDefault(p.getId(), Collections.emptyList())
                .stream()
                .map(topicMap::get)
                .filter(java.util.Objects::nonNull)
                .map(t -> new TopicDto(t.getId(), t.getTopicCode(), t.getTopicName(), null, false))
                .toList();
            out.add(new PostDto(
                p.getId(), p.getAuthorUserId(), p.getPostType(), p.getContentText(),
                p.getDanceStyleId(), p.getRelatedCourseId(), p.getRelatedWorkshopId(),
                p.getCityId(), p.getLocationName(), p.getLongitude(), p.getLatitude(),
                p.getVisibility(), p.getPostStatus(), p.getPublishedAt(),
                topics, lc, cc, likedSet.contains(p.getId())
            ));
        }
        return out;
    }

    private PostDto toPostDto(ContentPost p, Long currentUserId) {
        return enrichBatch(List.of(p), currentUserId).get(0);
    }

    private CommentDto toCommentDto(ContentComment c) {
        return new CommentDto(
            c.getId(), c.getContentPostId(), c.getUserId(),
            c.getParentCommentId(), c.getReplyToUserId(),
            c.getCommentText(), c.getCommentStatus(),
            c.getCreatedAt()
        );
    }
}
