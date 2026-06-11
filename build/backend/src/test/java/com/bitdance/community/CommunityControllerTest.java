package com.bitdance.community;

import com.bitdance.common.exception.BizException;
import com.bitdance.community.controller.CommunityController;
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
import com.bitdance.iam.jwt.JwtService;
import com.bitdance.profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommunityController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CommunityControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean CommunityService service;
    @MockBean ProfileService profileService;
    @MockBean JwtService jwtService;

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }

    private PostDto fixture() {
        return new PostDto(
            100L, 42L, "舞者0042", "", "note", "今日打卡：Hiphop 状态在线",
            1L, null, null, 1L, "海淀区舞星 Studio", null, null,
            "public", "published", OffsetDateTime.now(),
            List.of(new TopicDto(1L, "checkin", "零基础打卡", null, false)),
            List.of(new MediaAssetDto(9L, "image", "/api/public/community/media/9", "cover.png", "image/png", 10L, 0, OffsetDateTime.now())),
            5L, 2L, 1L, 3L, false, false
        );
    }

    // ============ Post ============

    @Test
    void create_returnsPost() throws Exception {
        when(service.createPost(eq(42L), any())).thenReturn(fixture());
        var body = new CreatePostRequest(
            "note", "今日打卡", null, null, null, 1L,
            "海淀区舞星 Studio", null, null, "public",
            List.of("零基础打卡"), List.of(9L)
        );
        mvc.perform(post("/h5/community/posts")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.postStatus").value("published"))
            .andExpect(jsonPath("$.data.topics[0].topicName").value("零基础打卡"));
    }

    @Test
    void create_blankText_returns400() throws Exception {
        var body = new CreatePostRequest("note", "", null, null, null, null,
            null, null, null, "public", null, null);
        mvc.perform(post("/h5/community/posts")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_invalidVisibility_returns400() throws Exception {
        var body = new CreatePostRequest("note", "x", null, null, null, null,
            null, null, null, "secret", null, null);
        mvc.perform(post("/h5/community/posts")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void create_tooManyTopics_returns400() throws Exception {
        var body = new CreatePostRequest("note", "x", null, null, null, null,
            null, null, null, "public",
            List.of("a", "b", "c", "d", "e", "f"), null);
        mvc.perform(post("/h5/community/posts")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void uploadMedia_returnsAsset() throws Exception {
        when(service.uploadMedia(eq(42L), any())).thenReturn(
            new MediaAssetDto(9L, "image", "/api/public/community/media/9", "cover.png", "image/png", 10L, 0, OffsetDateTime.now())
        );
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/h5/community/media")
                .file("file", "fake-image".getBytes())
                .header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mediaType").value("image"))
            .andExpect(jsonPath("$.data.url").value("/api/public/community/media/9"));
    }

    @Test
    void delete_otherUser_returnsForbidden() throws Exception {
        doThrow(new BizException("FORBIDDEN", "无权删除他人动态"))
            .when(service).deletePost(42L, 100L);
        mvc.perform(delete("/h5/community/posts/100").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void delete_ok() throws Exception {
        doNothing().when(service).deletePost(42L, 100L);
        mvc.perform(delete("/h5/community/posts/100").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void detail_notFound_returnsBiz() throws Exception {
        when(service.detail(eq(999L), any()))
            .thenThrow(new BizException("POST_NOT_FOUND", "动态不存在"));
        mvc.perform(get("/public/community/posts/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void detail_returns() throws Exception {
        when(service.detail(eq(100L), any())).thenReturn(fixture());
        mvc.perform(get("/public/community/posts/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.likeCount").value(5));
    }

    // ============ Feed ============

    @Test
    void feed_recommend_returnsList() throws Exception {
        when(service.feed(eq("recommend"), any(), any(), eq(1), eq(20), any()))
            .thenReturn(new PostListResponse(List.of(fixture()), 1, 20, 1L));
        mvc.perform(get("/public/community/feed").param("scope", "recommend"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void feed_follow_anonymous_returnsEmpty() throws Exception {
        when(service.feed(eq("follow"), any(), any(), eq(1), eq(20), eq(null)))
            .thenReturn(new PostListResponse(List.of(), 1, 20, 0L));
        mvc.perform(get("/public/community/feed").param("scope", "follow"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(0));
    }

    // ============ Like ============

    @Test
    void like_toggle_returnsTrue() throws Exception {
        when(service.toggleLike(42L, 100L)).thenReturn(Map.of("liked", true, "likeCount", 6L));
        mvc.perform(post("/h5/community/posts/100/like").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.liked").value(true))
            .andExpect(jsonPath("$.data.likeCount").value(6));
    }

    @Test
    void like_postNotFound_returnsBiz() throws Exception {
        when(service.toggleLike(42L, 999L))
            .thenThrow(new BizException("POST_NOT_FOUND", "动态不存在"));
        mvc.perform(post("/h5/community/posts/999/like").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void share_returnsCountAndUrl() throws Exception {
        when(service.sharePost(eq(42L), eq(100L), any()))
            .thenReturn(Map.of("shared", true, "shareCount", 4L, "shareUrl", "/community/post/100"));
        mvc.perform(post("/h5/community/posts/100/share")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SharePostRequest("link"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.shared").value(true))
            .andExpect(jsonPath("$.data.shareCount").value(4));
    }

    @Test
    void share_invalidChannel_returns400() throws Exception {
        mvc.perform(post("/h5/community/posts/100/share")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SharePostRequest("unknown"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    // ============ Comment ============

    @Test
    void createComment_returns() throws Exception {
        when(service.createComment(eq(42L), eq(100L), any())).thenReturn(new CommentDto(
            1L, 100L, 42L, null, null, "好棒", "published", OffsetDateTime.now()
        ));
        mvc.perform(post("/h5/community/posts/100/comments")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateCommentRequest("好棒", null, null))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.commentText").value("好棒"));
    }

    @Test
    void createReply_returnsParentFields() throws Exception {
        when(service.createComment(eq(42L), eq(100L), any())).thenReturn(new CommentDto(
            3L, 100L, 42L, 1L, 43L, "回复你", "published", OffsetDateTime.now()
        ));
        mvc.perform(post("/h5/community/posts/100/comments")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateCommentRequest("回复你", 1L, 43L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.parentCommentId").value(1))
            .andExpect(jsonPath("$.data.replyToUserId").value(43));
    }

    @Test
    void createComment_blank_returns400() throws Exception {
        mvc.perform(post("/h5/community/posts/100/comments")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateCommentRequest("", null, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void listComments_returns() throws Exception {
        when(service.listComments(100L)).thenReturn(List.of(
            new CommentDto(1L, 100L, 42L, null, null, "x", "published", OffsetDateTime.now()),
            new CommentDto(2L, 100L, 43L, null, null, "y", "published", OffsetDateTime.now())
        ));
        mvc.perform(get("/public/community/posts/100/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void deleteComment_otherUser_returnsForbidden() throws Exception {
        doThrow(new BizException("FORBIDDEN", "无权删除他人评论"))
            .when(service).deleteComment(42L, 1L);
        mvc.perform(delete("/h5/community/comments/1").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ============ Topic ============

    @Test
    void topics_list() throws Exception {
        when(service.listTopics(eq("hot"), eq(null), eq(20))).thenReturn(List.of(
            new TopicDto(1L, "checkin", "零基础打卡", 5L, true),
            new TopicDto(2L, "street", "街舞日常", 2L, false)
        ));
        mvc.perform(get("/public/community/topics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].hot").value(true));
    }

    @Test
    void createTopic_returnsTopic() throws Exception {
        when(service.createTopic(eq(42L), any())).thenReturn(
            new TopicDto(8L, "u-42-1", "Kpop打卡", 0L, false)
        );
        mvc.perform(post("/h5/community/topics")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CreateTopicRequest("Kpop打卡", "每日练习"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.topicName").value("Kpop打卡"));
    }

    @Test
    void topicDetail_returnsTopic() throws Exception {
        when(service.topicDetail("零基础打卡")).thenReturn(
            new TopicDto(1L, "checkin", "零基础打卡", 5L, true)
        );
        mvc.perform(get("/public/community/topics/{name}", "零基础打卡"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.postCount").value(5));
    }

    @Test
    void topicPosts_returns() throws Exception {
        when(service.postsByTopicName(eq("零基础打卡"), eq("hot"), eq(1), eq(20), any()))
            .thenReturn(new PostListResponse(List.of(fixture()), 1, 20, 1L));
        mvc.perform(get("/public/community/topics/{name}/posts", "零基础打卡"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1));
    }

    // ============ Follow ============

    @Test
    void follow_self_returns400() throws Exception {
        when(service.toggleFollow(42L, 42L))
            .thenThrow(new BizException("INVALID_ARGUMENT", "不能关注自己"));
        mvc.perform(post("/h5/community/follow/42").header("Authorization", "Bearer fake"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void follow_toggle_returns() throws Exception {
        when(service.toggleFollow(42L, 99L))
            .thenReturn(Map.of("following", true, "followerCount", 3L));
        mvc.perform(post("/h5/community/follow/99").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.following").value(true));
    }

    @Test
    void myFollowees_list() throws Exception {
        when(service.listFollowees(42L)).thenReturn(List.of(11L, 22L, 33L));
        mvc.perform(get("/h5/community/follow/me").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void followingUsers_list() throws Exception {
        when(service.listFollowingUsers(42L)).thenReturn(List.of(
            new FollowUserDto(99L, "小鹿", "", true, 3L, 2L, OffsetDateTime.now())
        ));
        mvc.perform(get("/h5/community/follow/following").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].userId").value(99))
            .andExpect(jsonPath("$.data[0].following").value(true));
    }

    @Test
    void followers_list() throws Exception {
        when(service.listFollowerUsers(42L)).thenReturn(List.of(
            new FollowUserDto(88L, "Aki", "", false, 1L, 4L, OffsetDateTime.now())
        ));
        mvc.perform(get("/h5/community/follow/followers").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].userId").value(88));
    }

    @Test
    void followStatus_returnsCounts() throws Exception {
        when(service.followStatus(42L, 99L)).thenReturn(Map.of(
            "userId", 99L, "following", true, "followerCount", 3L, "followeeCount", 2L
        ));
        mvc.perform(get("/h5/community/follow/99/status").header("Authorization", "Bearer fake"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.following").value(true))
            .andExpect(jsonPath("$.data.followerCount").value(3));
    }

    // ============ Report ============

    @Test
    void reportPost_ok() throws Exception {
        when(service.reportPost(eq(42L), eq(100L), any()))
            .thenReturn(Map.of("reported", true, "ticketId", 9001L));
        mvc.perform(post("/h5/community/posts/100/report")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ReportRequest("spam", "广告"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ticketId").value(9001));
    }

    @Test
    void reportPost_duplicated_returnsBiz() throws Exception {
        when(service.reportPost(eq(42L), eq(100L), any()))
            .thenThrow(new BizException("REPORT_DUPLICATED", "已存在未处理的举报"));
        mvc.perform(post("/h5/community/posts/100/report")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ReportRequest("spam", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REPORT_DUPLICATED"));
    }

    @Test
    void reportPost_invalidReason_returns400() throws Exception {
        mvc.perform(post("/h5/community/posts/100/report")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ReportRequest("politics", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void reportComment_ok() throws Exception {
        when(service.reportComment(eq(42L), eq(1L), any()))
            .thenReturn(Map.of("reported", true, "ticketId", 9002L));
        mvc.perform(post("/h5/community/comments/1/report")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ReportRequest("spam", "评论引流"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reported").value(true))
            .andExpect(jsonPath("$.data.ticketId").value(9002));
    }

    // ============ Search & 未登录 ============

    @Test
    void search_returns() throws Exception {
        when(service.search(eq("打卡"), eq(1), eq(20), any()))
            .thenReturn(new PostListResponse(List.of(fixture()), 1, 20, 1L));
        mvc.perform(get("/public/community/search").param("q", "打卡"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void like_withoutToken_returnsUnauthorized() throws Exception {
        mvc.perform(post("/h5/community/posts/100/like"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
