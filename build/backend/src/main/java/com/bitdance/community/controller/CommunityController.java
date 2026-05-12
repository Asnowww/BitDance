package com.bitdance.community.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.community.dto.CommentDto;
import com.bitdance.community.dto.CreateCommentRequest;
import com.bitdance.community.dto.CreatePostRequest;
import com.bitdance.community.dto.PostDto;
import com.bitdance.community.dto.PostListResponse;
import com.bitdance.community.dto.ReportRequest;
import com.bitdance.community.dto.TopicDto;
import com.bitdance.community.service.CommunityService;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class CommunityController {

    private final CommunityService service;

    public CommunityController(CommunityService service) {
        this.service = service;
    }

    // ---------- Post ----------

    @PostMapping("/h5/community/posts")
    public ApiResponse<PostDto> create(@Valid @RequestBody CreatePostRequest body) {
        return ApiResponse.ok(service.createPost(CurrentUser.getId(), body));
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

    // ---------- Like ----------

    @PostMapping("/h5/community/posts/{id}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long id) {
        return ApiResponse.ok(service.toggleLike(CurrentUser.getId(), id));
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
    public ApiResponse<List<TopicDto>> topics() {
        return ApiResponse.ok(service.listTopics());
    }

    @GetMapping("/public/community/topics/{name}/posts")
    public ApiResponse<PostListResponse> postsByTopic(
        @PathVariable String name,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.postsByTopicName(name, page, pageSize, CurrentUser.getIdOrNull()));
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
