package com.bitdance.community.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.community.domain.ContentComment;
import com.bitdance.community.domain.ContentLike;
import com.bitdance.community.domain.ContentPostMedia;
import com.bitdance.community.domain.ContentPost;
import com.bitdance.community.domain.ContentPostTopic;
import com.bitdance.community.domain.ContentShareLog;
import com.bitdance.community.domain.FollowRelation;
import com.bitdance.community.domain.ReportTicket;
import com.bitdance.community.domain.TopicTag;
import com.bitdance.community.dto.CommentDto;
import com.bitdance.community.dto.CreateCommentRequest;
import com.bitdance.community.dto.CreatePostRequest;
import com.bitdance.community.dto.CreateTopicRequest;
import com.bitdance.community.dto.FollowUserDto;
import com.bitdance.community.dto.MediaAssetDto;
import com.bitdance.community.dto.PostDto;
import com.bitdance.community.dto.PostListResponse;
import com.bitdance.community.dto.ReportRequest;
import com.bitdance.community.dto.SharePostRequest;
import com.bitdance.community.dto.TopicDto;
import com.bitdance.community.repository.ContentCommentRepository;
import com.bitdance.community.repository.ContentLikeRepository;
import com.bitdance.community.repository.ContentPostMediaRepository;
import com.bitdance.community.repository.ContentPostRepository;
import com.bitdance.community.repository.ContentPostTopicRepository;
import com.bitdance.community.repository.ContentShareLogRepository;
import com.bitdance.community.repository.FollowRelationRepository;
import com.bitdance.community.repository.ReportTicketRepository;
import com.bitdance.community.repository.TopicTagRepository;
import com.bitdance.favorite.repository.FavoriteRepository;
import com.bitdance.profile.domain.UserProfile;
import com.bitdance.profile.repository.UserProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommunityService {

    private static final Set<String> ALLOWED_REPORT_TARGET_TYPES = Set.of(
        "content_post", "content_comment"
    );
    private static final String FAV_TARGET = "content_post";
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 80L * 1024 * 1024;
    private static final int MAX_MEDIA_COUNT = 10;

    private final ContentPostRepository postRepo;
    private final ContentCommentRepository commentRepo;
    private final ContentLikeRepository likeRepo;
    private final ContentPostMediaRepository mediaRepo;
    private final ContentShareLogRepository shareRepo;
    private final TopicTagRepository topicRepo;
    private final ContentPostTopicRepository postTopicRepo;
    private final FollowRelationRepository followRepo;
    private final ReportTicketRepository reportRepo;
    private final FavoriteRepository favoriteRepo;
    private final UserProfileRepository profileRepo;

    public CommunityService(
        ContentPostRepository postRepo,
        ContentCommentRepository commentRepo,
        ContentLikeRepository likeRepo,
        ContentPostMediaRepository mediaRepo,
        ContentShareLogRepository shareRepo,
        TopicTagRepository topicRepo,
        ContentPostTopicRepository postTopicRepo,
        FollowRelationRepository followRepo,
        ReportTicketRepository reportRepo,
        FavoriteRepository favoriteRepo,
        UserProfileRepository profileRepo
    ) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.likeRepo = likeRepo;
        this.mediaRepo = mediaRepo;
        this.shareRepo = shareRepo;
        this.topicRepo = topicRepo;
        this.postTopicRepo = postTopicRepo;
        this.followRepo = followRepo;
        this.reportRepo = reportRepo;
        this.favoriteRepo = favoriteRepo;
        this.profileRepo = profileRepo;
    }

    // ============ Post ============

    @Transactional
    public MediaAssetDto uploadMedia(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("INVALID_ARGUMENT", "请选择要上传的图片或视频");
        }
        String mime = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
        String type;
        long maxBytes;
        if (mime.startsWith("image/")) {
            type = "image";
            maxBytes = MAX_IMAGE_BYTES;
        } else if (mime.startsWith("video/")) {
            type = "video";
            maxBytes = MAX_VIDEO_BYTES;
        } else {
            throw new BizException("INVALID_ARGUMENT", "仅支持图片或视频文件");
        }
        if (file.getSize() > maxBytes) {
            throw new BizException("MEDIA_TOO_LARGE", "媒体文件过大");
        }
        ContentPostMedia media = new ContentPostMedia();
        media.setOwnerUserId(userId);
        media.setMediaType(type);
        media.setOriginalFilename(cleanFilename(file.getOriginalFilename()));
        media.setMimeType(mime);
        media.setFileSize(file.getSize());
        media.setMediaStatus("draft");
        media.setSortOrder(0);
        try {
            media.setMediaData(file.getBytes());
        } catch (IOException ex) {
            throw new BizException("MEDIA_READ_FAILED", "读取媒体文件失败");
        }
        return toMediaDto(mediaRepo.save(media));
    }

    @Transactional(readOnly = true)
    public ContentPostMedia getPublishedMedia(Long mediaId, Long currentUserId) {
        ContentPostMedia media = mediaRepo.findById(mediaId)
            .orElseThrow(() -> new BizException("MEDIA_NOT_FOUND", "媒体不存在"));
        if (!"active".equals(media.getMediaStatus()) || media.getContentPostId() == null) {
            throw new BizException("MEDIA_NOT_FOUND", "媒体不存在");
        }
        ContentPost post = postRepo.findById(media.getContentPostId())
            .orElseThrow(() -> new BizException("MEDIA_NOT_FOUND", "媒体不存在"));
        if (!canViewPublishedPost(post, currentUserId)) {
            throw new BizException("MEDIA_NOT_FOUND", "媒体不存在");
        }
        return media;
    }

    @Transactional
    @CacheEvict(cacheNames = "community:feed:recommend", allEntries = true)
    public PostDto createPost(Long userId, CreatePostRequest req) {
        ContentPost p = new ContentPost();
        p.setAuthorUserId(userId);
        List<ContentPostMedia> media = validateMediaSelection(userId, null, req.mediaAssetIds());
        p.setPostType(resolvePostType(req.postType(), media));
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

        replaceTopics(saved.getId(), userId, req.topicNames());
        attachMedia(saved.getId(), userId, media);
        return toPostDto(saved, userId);
    }

    @Transactional
    @CacheEvict(cacheNames = "community:feed:recommend", allEntries = true)
    public PostDto updatePost(Long userId, Long postId, CreatePostRequest req) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!p.getAuthorUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权编辑他人动态");
        }
        if (!"published".equals(p.getPostStatus()) && !"draft".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        List<ContentPostMedia> media = validateMediaSelection(userId, postId, req.mediaAssetIds());
        p.setPostType(resolvePostType(req.postType(), media));
        p.setContentText(req.contentText());
        p.setDanceStyleId(req.danceStyleId());
        p.setRelatedCourseId(req.relatedCourseId());
        p.setRelatedWorkshopId(req.relatedWorkshopId());
        p.setCityId(req.cityId());
        p.setLocationName(req.locationName());
        p.setLongitude(req.longitude());
        p.setLatitude(req.latitude());
        p.setVisibility(req.visibility() == null ? "public" : req.visibility());
        ContentPost saved = postRepo.save(p);
        replaceTopics(saved.getId(), userId, req.topicNames());
        replaceMedia(saved.getId(), userId, media);
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
        for (ContentPostMedia media : mediaRepo.findByContentPostIdAndMediaStatusOrderBySortOrderAscIdAsc(postId, "active")) {
            media.setMediaStatus("deleted");
            mediaRepo.save(media);
        }
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
            List<Long> followees = followeeIds(currentUserId);
            if (followees.isEmpty()) {
                return new PostListResponse(List.of(), safePage, safeSize, 0L);
            }
            p = postRepo.followingFeed(followees, danceStyleId, pr);
        } else {
            List<Long> followees = currentUserId == null ? List.of() : followeeIds(currentUserId);
            p = followees.isEmpty()
                ? postRepo.recommend(danceStyleId, pr)
                : postRepo.recommendPrioritized(danceStyleId, followees, pr);
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

    @Transactional(readOnly = true)
    public PostListResponse publicPostsByAuthor(
        Long authorUserId, int page, int pageSize, Long currentUserId
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<ContentPost> p = postRepo.publicPostsByAuthor(
            authorUserId, PageRequest.of(safePage - 1, safeSize));
        return new PostListResponse(enrichBatch(p.getContent(), currentUserId),
            safePage, safeSize, p.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostListResponse myPosts(Long userId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        Page<ContentPost> p = postRepo.postsByAuthorForOwner(
            userId, PageRequest.of(safePage - 1, safeSize));
        return new PostListResponse(enrichBatch(p.getContent(), userId),
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

    @Transactional
    public Map<String, Object> toggleCollect(Long userId, Long postId) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!"published".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        boolean collected = favoriteRepo.findByUserIdAndTargetTypeAndTargetId(userId, FAV_TARGET, postId)
            .map(existing -> {
                favoriteRepo.delete(existing);
                return false;
            })
            .orElseGet(() -> {
                com.bitdance.favorite.domain.Favorite f = new com.bitdance.favorite.domain.Favorite();
                f.setUserId(userId);
                f.setTargetType(FAV_TARGET);
                f.setTargetId(postId);
                favoriteRepo.save(f);
                return true;
            });
        long count = favoriteRepo.countByTargetTypeAndTargetId(FAV_TARGET, postId);
        return Map.of("collected", collected, "collectCount", count);
    }

    @Transactional
    public Map<String, Object> sharePost(Long userId, Long postId, SharePostRequest req) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!"published".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        if ("private".equals(p.getVisibility()) && !p.getAuthorUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "无权分享私密动态");
        }
        ContentShareLog log = new ContentShareLog();
        log.setContentPostId(postId);
        log.setUserId(userId);
        log.setShareChannel(req.channel() == null ? "link" : req.channel());
        shareRepo.save(log);
        long count = shareRepo.countByContentPostId(postId);
        return Map.of(
            "shared", true,
            "shareCount", count,
            "shareUrl", "/community/post/" + postId
        );
    }

    // ============ Comment ============

    @Transactional
    public CommentDto createComment(Long userId, Long postId, CreateCommentRequest req) {
        ContentPost p = postRepo.findById(postId)
            .orElseThrow(() -> new BizException("POST_NOT_FOUND", "动态不存在"));
        if (!"published".equals(p.getPostStatus())) {
            throw new BizException("POST_NOT_FOUND", "动态不存在");
        }
        Long parentCommentId = req.parentCommentId();
        Long replyToUserId = req.replyToUserId();
        if (parentCommentId != null) {
            ContentComment parent = commentRepo.findById(parentCommentId)
                .orElseThrow(() -> new BizException("COMMENT_NOT_FOUND", "评论不存在"));
            if (!parent.getContentPostId().equals(postId) || !"published".equals(parent.getCommentStatus())) {
                throw new BizException("INVALID_ARGUMENT", "回复的评论不属于当前动态");
            }
            if (replyToUserId == null) {
                replyToUserId = parent.getUserId();
            }
        }
        ContentComment c = new ContentComment();
        c.setContentPostId(postId);
        c.setUserId(userId);
        c.setParentCommentId(parentCommentId);
        c.setReplyToUserId(replyToUserId);
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
    public List<TopicDto> listTopics(String scope, String q, int limit) {
        int safeLimit = Math.min(Math.max(1, limit), 50);
        String keyword = q == null || q.isBlank() ? null : normalizeTopicName(q);
        if ("new".equals(scope)) {
            return topicRepo.findByStatusOrderByIdDesc("active").stream()
                .filter(t -> keyword == null || t.getTopicName().toLowerCase().contains(keyword.toLowerCase()))
                .limit(safeLimit)
                .map(this::toTopicDto)
                .toList();
        }
        return topicRepo.findActiveWithPostCount(keyword, PageRequest.of(0, safeLimit)).stream()
            .map(row -> toTopicDto((TopicTag) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    @Transactional
    public TopicDto createTopic(Long userId, CreateTopicRequest req) {
        String name = normalizeTopicName(req.topicName());
        if (name.isBlank()) {
            throw new BizException("INVALID_ARGUMENT", "话题名称不能为空");
        }
        return topicRepo.findByTopicNameAndStatus(name, "active")
            .map(this::toTopicDto)
            .orElseGet(() -> {
                TopicTag t = new TopicTag();
                t.setTopicCode(newTopicCode(userId, name));
                t.setTopicName(name);
                t.setDescription(req.description() == null || req.description().isBlank()
                    ? null
                    : req.description().trim());
                t.setCreatorUserId(userId);
                t.setIsSystem(false);
                t.setStatus("active");
                return toTopicDto(topicRepo.save(t));
            });
    }

    @Transactional(readOnly = true)
    public TopicDto topicDetail(String topicNameOrCode) {
        TopicTag t = findActiveTopic(topicNameOrCode);
        return toTopicDto(t);
    }

    @Transactional(readOnly = true)
    public PostListResponse postsByTopicName(
        String topicName, String sort, int page, int pageSize, Long currentUserId
    ) {
        TopicTag t = findActiveTopic(topicName);
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        PageRequest pr = PageRequest.of(safePage - 1, safeSize);
        Page<ContentPost> p = "new".equals(sort)
            ? postRepo.byTopic(t.getId(), pr)
            : postRepo.byTopicHot(t.getId(), pr);
        return new PostListResponse(enrichBatch(p.getContent(), currentUserId),
            safePage, safeSize, p.getTotalElements());
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
            "followerCount", followRepo.countByFolloweeUserId(followeeId),
            "followeeCount", followRepo.countByFollowerUserId(followerId)
        );
    }

    @Transactional(readOnly = true)
    public List<Long> listFollowees(Long userId) {
        return followeeIds(userId);
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> listFollowingUsers(Long userId) {
        return followRepo.findByFollowerUserId(userId).stream()
            .sorted(Comparator.comparing(FollowRelation::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(r -> toFollowUserDto(r.getFolloweeUserId(), userId, r.getCreatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> listFollowerUsers(Long userId) {
        return followRepo.findByFolloweeUserId(userId).stream()
            .sorted(Comparator.comparing(FollowRelation::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(r -> toFollowUserDto(r.getFollowerUserId(), userId, r.getCreatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> followStatus(Long currentUserId, Long targetUserId) {
        return Map.of(
            "userId", targetUserId,
            "following", followRepo.existsByFollowerUserIdAndFolloweeUserId(currentUserId, targetUserId),
            "followerCount", followRepo.countByFolloweeUserId(targetUserId),
            "followeeCount", followRepo.countByFollowerUserId(targetUserId)
        );
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
        Map<Long, Long> collectCounts = new HashMap<>();
        for (Map<String, Object> row : favoriteRepo.countGroupedByTargetIds(FAV_TARGET, ids)) {
            collectCounts.put(((Number) row.get("targetId")).longValue(),
                ((Number) row.get("cnt")).longValue());
        }
        Map<Long, Long> shareCounts = new HashMap<>();
        for (Map<String, Object> row : shareRepo.countGroupedByPostIds(ids)) {
            shareCounts.put(((Number) row.get("postId")).longValue(),
                ((Number) row.get("cnt")).longValue());
        }
        Set<Long> likedSet = currentUserId == null
            ? Set.of()
            : new HashSet<>(likeRepo.findLikedPostIds(currentUserId, ids));
        Set<Long> collectedSet = currentUserId == null
            ? Set.of()
            : new HashSet<>(favoriteRepo.findFavoredIds(currentUserId, FAV_TARGET, ids));

        Map<Long, List<Long>> postIdToTopicIds = new HashMap<>();
        for (Map<String, Object> row : postTopicRepo.findByPostIds(ids)) {
            Long postId = ((Number) row.get("postId")).longValue();
            Long topicId = ((Number) row.get("topicId")).longValue();
            postIdToTopicIds.computeIfAbsent(postId, k -> new ArrayList<>()).add(topicId);
        }

        Map<Long, List<MediaAssetDto>> postIdToMedia = new HashMap<>();
        for (ContentPostMedia media : mediaRepo.findByContentPostIdInAndMediaStatusOrderBySortOrderAscIdAsc(ids, "active")) {
            postIdToMedia.computeIfAbsent(media.getContentPostId(), k -> new ArrayList<>()).add(toMediaDto(media));
        }
        Set<Long> allTopicIds = new HashSet<>();
        postIdToTopicIds.values().forEach(allTopicIds::addAll);
        Map<Long, TopicTag> topicMap = new HashMap<>();
        if (!allTopicIds.isEmpty()) {
            for (TopicTag t : topicRepo.findByIdIn(new ArrayList<>(allTopicIds))) {
                topicMap.put(t.getId(), t);
            }
        }
        Map<Long, UserProfile> profileMap = new HashMap<>();
        Set<Long> authorIds = new HashSet<>();
        posts.forEach(p -> authorIds.add(p.getAuthorUserId()));
        if (!authorIds.isEmpty()) {
            for (UserProfile profile : profileRepo.findAllById(authorIds)) {
                profileMap.put(profile.getUserId(), profile);
            }
        }

        List<PostDto> out = new ArrayList<>(posts.size());
        for (ContentPost p : posts) {
            long lc = likeCounts.getOrDefault(p.getId(), 0L);
            long cc = commentCounts.getOrDefault(p.getId(), 0L);
            long fc = collectCounts.getOrDefault(p.getId(), 0L);
            long sc = shareCounts.getOrDefault(p.getId(), 0L);
            List<TopicDto> topics = postIdToTopicIds
                .getOrDefault(p.getId(), Collections.emptyList())
                .stream()
                .map(topicMap::get)
                .filter(java.util.Objects::nonNull)
                .map(t -> new TopicDto(t.getId(), t.getTopicCode(), t.getTopicName(), null, false))
                .toList();
            UserProfile author = profileMap.get(p.getAuthorUserId());
            out.add(new PostDto(
                p.getId(), p.getAuthorUserId(), displayName(p.getAuthorUserId(), author), "",
                p.getPostType(), p.getContentText(),
                p.getDanceStyleId(), p.getRelatedCourseId(), p.getRelatedWorkshopId(),
                p.getCityId(), p.getLocationName(), p.getLongitude(), p.getLatitude(),
                p.getVisibility(), p.getPostStatus(), p.getPublishedAt(),
                topics, postIdToMedia.getOrDefault(p.getId(), List.of()),
                lc, cc, fc, sc, likedSet.contains(p.getId()), collectedSet.contains(p.getId())
            ));
        }
        return out;
    }

    private void replaceTopics(Long postId, Long userId, List<String> topicNames) {
        postTopicRepo.deleteAll(postTopicRepo.findByContentPostId(postId));
        if (topicNames == null || topicNames.isEmpty()) return;
        for (String name : new LinkedHashSet<>(topicNames)) {
            if (name == null || name.isBlank()) continue;
            String normalized = normalizeTopicName(name);
            if (normalized.isBlank()) continue;
            TopicTag tag = topicRepo.findByTopicName(normalized).orElseGet(() -> {
                TopicTag t = new TopicTag();
                t.setTopicCode(newTopicCode(userId, normalized));
                t.setTopicName(normalized);
                t.setCreatorUserId(userId);
                t.setIsSystem(false);
                t.setStatus("active");
                return topicRepo.save(t);
            });
            ContentPostTopic link = new ContentPostTopic();
            link.setContentPostId(postId);
            link.setTopicTagId(tag.getId());
            postTopicRepo.save(link);
        }
    }

    private TopicTag findActiveTopic(String topicNameOrCode) {
        String normalized = normalizeTopicName(topicNameOrCode);
        return topicRepo.findByTopicNameAndStatus(normalized, "active")
            .or(() -> topicRepo.findByTopicCodeAndStatus(normalized, "active"))
            .orElseThrow(() -> new BizException("TOPIC_NOT_FOUND", "话题不存在"));
    }

    private TopicDto toTopicDto(TopicTag t) {
        return toTopicDto(t, postTopicRepo.countByTopicId(t.getId()));
    }

    private TopicDto toTopicDto(TopicTag t, long count) {
        return new TopicDto(t.getId(), t.getTopicCode(), t.getTopicName(), count, count >= 4);
    }

    private String normalizeTopicName(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceFirst("^#+", "").trim();
    }

    private String newTopicCode(Long userId, String normalizedName) {
        long suffix = Math.abs((normalizedName + "-" + System.nanoTime()).hashCode() % 100000);
        return "u-" + userId + "-" + System.currentTimeMillis() + "-" + suffix;
    }

    private List<Long> followeeIds(Long userId) {
        return followRepo.findByFollowerUserId(userId)
            .stream().map(FollowRelation::getFolloweeUserId).toList();
    }

    private boolean canViewPublishedPost(ContentPost post, Long currentUserId) {
        if (!"published".equals(post.getPostStatus())) {
            return false;
        }
        if ("public".equals(post.getVisibility())) {
            return true;
        }
        if (currentUserId != null && currentUserId.equals(post.getAuthorUserId())) {
            return true;
        }
        return "followers".equals(post.getVisibility())
            && currentUserId != null
            && followRepo.existsByFollowerUserIdAndFolloweeUserId(currentUserId, post.getAuthorUserId());
    }

    private FollowUserDto toFollowUserDto(Long targetUserId, Long viewerUserId, OffsetDateTime followedAt) {
        String name = profileRepo.findById(targetUserId)
            .map(profile -> displayName(targetUserId, profile))
            .orElseGet(() -> fallbackDisplayName(targetUserId));
        return new FollowUserDto(
            targetUserId,
            name,
            "",
            followRepo.existsByFollowerUserIdAndFolloweeUserId(viewerUserId, targetUserId),
            followRepo.countByFolloweeUserId(targetUserId),
            followRepo.countByFollowerUserId(targetUserId),
            followedAt
        );
    }

    private String displayName(Long userId, UserProfile profile) {
        if (profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()) {
            return profile.getNickname();
        }
        return fallbackDisplayName(userId);
    }

    private String fallbackDisplayName(Long userId) {
        String raw = String.valueOf(userId == null ? 0 : userId);
        return "舞者" + raw.substring(Math.max(0, raw.length() - 4));
    }

    private List<ContentPostMedia> validateMediaSelection(Long userId, Long editingPostId, List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) return List.of();
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(mediaIds));
        if (uniqueIds.size() > MAX_MEDIA_COUNT) {
            throw new BizException("MEDIA_TOO_MANY", "最多添加 9 张图片或 1 个视频");
        }
        List<ContentPostMedia> media = mediaRepo.findByIdIn(uniqueIds);
        if (media.size() != uniqueIds.size()) {
            throw new BizException("MEDIA_NOT_FOUND", "部分媒体不存在");
        }
        media.sort(Comparator.comparingInt(m -> uniqueIds.indexOf(m.getId())));
        long videoCount = media.stream().filter(m -> "video".equals(m.getMediaType())).count();
        long imageCount = media.stream().filter(m -> "image".equals(m.getMediaType())).count();
        if (videoCount > 1 || (videoCount > 0 && imageCount > 0) || imageCount > 9) {
            throw new BizException("INVALID_ARGUMENT", "图文动态最多 9 张图片，视频动态只能上传 1 个视频");
        }
        for (ContentPostMedia item : media) {
            if (!item.getOwnerUserId().equals(userId)) {
                throw new BizException("FORBIDDEN", "无权使用他人媒体");
            }
            boolean keepingCurrentPostMedia = editingPostId != null
                && "active".equals(item.getMediaStatus())
                && editingPostId.equals(item.getContentPostId());
            if (!"draft".equals(item.getMediaStatus()) && !keepingCurrentPostMedia) {
                throw new BizException("MEDIA_NOT_FOUND", "媒体不可用");
            }
        }
        return media;
    }

    private void attachMedia(Long postId, Long userId, List<ContentPostMedia> media) {
        for (int i = 0; i < media.size(); i += 1) {
            ContentPostMedia item = media.get(i);
            if (!item.getOwnerUserId().equals(userId)) {
                throw new BizException("FORBIDDEN", "无权使用他人媒体");
            }
            item.setContentPostId(postId);
            item.setSortOrder(i);
            item.setMediaStatus("active");
            mediaRepo.save(item);
        }
    }

    private void replaceMedia(Long postId, Long userId, List<ContentPostMedia> nextMedia) {
        Set<Long> nextIds = nextMedia.stream().map(ContentPostMedia::getId).collect(java.util.stream.Collectors.toSet());
        for (ContentPostMedia existing : mediaRepo.findByContentPostIdAndMediaStatusOrderBySortOrderAscIdAsc(postId, "active")) {
            if (!nextIds.contains(existing.getId())) {
                existing.setMediaStatus("deleted");
                mediaRepo.save(existing);
            }
        }
        attachMedia(postId, userId, nextMedia);
    }

    private String resolvePostType(String requested, List<ContentPostMedia> media) {
        boolean hasVideo = media.stream().anyMatch(m -> "video".equals(m.getMediaType()));
        if (hasVideo) return "video";
        return requested == null ? "note" : requested;
    }

    private String cleanFilename(String filename) {
        if (filename == null || filename.isBlank()) return "media";
        return filename.replaceAll("[\\\\/\\r\\n]", "_");
    }

    private MediaAssetDto toMediaDto(ContentPostMedia media) {
        return new MediaAssetDto(
            media.getId(),
            media.getMediaType(),
            "/api/public/community/media/" + media.getId(),
            media.getOriginalFilename(),
            media.getMimeType(),
            media.getFileSize(),
            media.getSortOrder(),
            media.getCreatedAt()
        );
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
