package com.bitdance.community.controller;

import com.bitdance.common.web.ApiResponse;
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
import com.bitdance.community.service.CommunityService;
import com.bitdance.community.domain.ContentPostMedia;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class CommunityController {

    private final CommunityService service;
    private final ProfileService profileService;

    public CommunityController(CommunityService service, ProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    // ---------- Post ----------

    @PostMapping(value = "/h5/community/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAssetDto> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(service.uploadMedia(CurrentUser.getId(), file));
    }

    @GetMapping("/public/community/media/{id}")
    public ResponseEntity<byte[]> media(@PathVariable Long id) {
        ContentPostMedia media = service.getPublishedMedia(id, CurrentUser.getIdOrNull());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(media.getMimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(media.getMediaData());
    }

    @PostMapping("/h5/community/posts")
    public ApiResponse<PostDto> create(@Valid @RequestBody CreatePostRequest body) {
        return ApiResponse.ok(service.createPost(CurrentUser.getId(), body));
    }

    @PutMapping("/h5/community/posts/{id}")
    public ApiResponse<PostDto> update(@PathVariable Long id, @Valid @RequestBody CreatePostRequest body) {
        return ApiResponse.ok(service.updatePost(CurrentUser.getId(), id, body));
    }

    @DeleteMapping("/h5/community/posts/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        service.deletePost(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @GetMapping("/public/community/posts/{id}")
    public ApiResponse<PostDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/public/community/feed")
    public ApiResponse<PostListResponse> feed(
        @RequestParam(defaultValue = "recommend") String scope,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(required = false) Long topicId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.feed(scope, danceStyleId, topicId,
            page, pageSize, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/public/community/search")
    public ApiResponse<PostListResponse> search(
        @RequestParam String q,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.search(q, page, pageSize, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/public/users/{userId}/community/posts")
    public ApiResponse<PostListResponse> publicPostsByUser(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        if (!profileService.canViewContent(userId, CurrentUser.getIdOrNull())) {
            return ApiResponse.ok(new PostListResponse(List.of(), Math.max(1, page), Math.max(1, pageSize), 0L));
        }
        return ApiResponse.ok(service.publicPostsByAuthor(
            userId, page, pageSize, CurrentUser.getIdOrNull()));
    }

    @GetMapping("/h5/community/posts/me")
    public ApiResponse<PostListResponse> myPosts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.myPosts(CurrentUser.getId(), page, pageSize));
    }

    // ---------- Like ----------

    @PostMapping("/h5/community/posts/{id}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long id) {
        return ApiResponse.ok(service.toggleLike(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/community/posts/{id}/collect")
    public ApiResponse<Map<String, Object>> collect(@PathVariable Long id) {
        return ApiResponse.ok(service.toggleCollect(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/community/posts/{id}/share")
    public ApiResponse<Map<String, Object>> share(
        @PathVariable Long id,
        @Valid @RequestBody SharePostRequest body
    ) {
        return ApiResponse.ok(service.sharePost(CurrentUser.getId(), id, body));
    }

    // ---------- Comment ----------

    @PostMapping("/h5/community/posts/{id}/comments")
    public ApiResponse<CommentDto> createComment(
        @PathVariable Long id,
        @Valid @RequestBody CreateCommentRequest body
    ) {
        return ApiResponse.ok(service.createComment(CurrentUser.getId(), id, body));
    }

    @GetMapping("/public/community/posts/{id}/comments")
    public ApiResponse<List<CommentDto>> listComments(@PathVariable Long id) {
        return ApiResponse.ok(service.listComments(id));
    }

    @DeleteMapping("/h5/community/comments/{commentId}")
    public ApiResponse<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        service.deleteComment(CurrentUser.getId(), commentId);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    // ---------- Topic ----------

    @GetMapping("/public/community/topics")
    public ApiResponse<List<TopicDto>> topics(
        @RequestParam(defaultValue = "hot") String scope,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(service.listTopics(scope, q, limit));
    }

    @PostMapping("/h5/community/topics")
    public ApiResponse<TopicDto> createTopic(@Valid @RequestBody CreateTopicRequest body) {
        return ApiResponse.ok(service.createTopic(CurrentUser.getId(), body));
    }

    @GetMapping("/public/community/topics/{name}")
    public ApiResponse<TopicDto> topicDetail(@PathVariable String name) {
        return ApiResponse.ok(service.topicDetail(name));
    }

    @GetMapping("/public/community/topics/{name}/posts")
    public ApiResponse<PostListResponse> postsByTopic(
        @PathVariable String name,
        @RequestParam(defaultValue = "hot") String sort,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.postsByTopicName(name, sort, page, pageSize, CurrentUser.getIdOrNull()));
    }

    // ---------- Follow ----------

    @PostMapping("/h5/community/follow/{userId}")
    public ApiResponse<Map<String, Object>> toggleFollow(@PathVariable Long userId) {
        return ApiResponse.ok(service.toggleFollow(CurrentUser.getId(), userId));
    }

    @GetMapping("/h5/community/follow/me")
    public ApiResponse<List<Long>> myFollowees() {
        return ApiResponse.ok(service.listFollowees(CurrentUser.getId()));
    }

    @GetMapping("/h5/community/follow/following")
    public ApiResponse<List<FollowUserDto>> followingUsers() {
        return ApiResponse.ok(service.listFollowingUsers(CurrentUser.getId()));
    }

    @GetMapping("/h5/community/follow/followers")
    public ApiResponse<List<FollowUserDto>> followerUsers() {
        return ApiResponse.ok(service.listFollowerUsers(CurrentUser.getId()));
    }

    @GetMapping("/h5/community/follow/{userId}/status")
    public ApiResponse<Map<String, Object>> followStatus(@PathVariable Long userId) {
        return ApiResponse.ok(service.followStatus(CurrentUser.getId(), userId));
    }

    // ---------- Report ----------

    @PostMapping("/h5/community/posts/{id}/report")
    public ApiResponse<Map<String, Object>> reportPost(
        @PathVariable Long id,
        @Valid @RequestBody ReportRequest body
    ) {
        return ApiResponse.ok(service.reportPost(CurrentUser.getId(), id, body));
    }

    @PostMapping("/h5/community/comments/{commentId}/report")
    public ApiResponse<Map<String, Object>> reportComment(
        @PathVariable Long commentId,
        @Valid @RequestBody ReportRequest body
    ) {
        return ApiResponse.ok(service.reportComment(CurrentUser.getId(), commentId, body));
    }
}
