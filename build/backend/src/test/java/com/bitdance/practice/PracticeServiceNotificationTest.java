package com.bitdance.practice;

import com.bitdance.buddy.repository.PracticeRatingRepository;
import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.practice.domain.PracticeJoinRequest;
import com.bitdance.practice.domain.PracticePost;
import com.bitdance.practice.dto.JoinPracticeRequest;
import com.bitdance.practice.repository.PracticeCompletionConfirmRepository;
import com.bitdance.practice.repository.PracticeJoinRequestRepository;
import com.bitdance.practice.repository.PracticePostRepository;
import com.bitdance.practice.service.PracticeService;
import com.bitdance.profile.repository.UserDancePreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeServiceNotificationTest {

    private final PracticePostRepository postRepo = mock(PracticePostRepository.class);
    private final PracticeJoinRequestRepository joinRepo = mock(PracticeJoinRequestRepository.class);
    private final UserDancePreferenceRepository preferenceRepo = mock(UserDancePreferenceRepository.class);
    private final PracticeCompletionConfirmRepository completionRepo = mock(PracticeCompletionConfirmRepository.class);
    private final PracticeRatingRepository ratingRepo = mock(PracticeRatingRepository.class);
    private final NotificationRepository notificationRepo = mock(NotificationRepository.class);

    private final PracticeService service = new PracticeService(
        postRepo, joinRepo, preferenceRepo, completionRepo, ratingRepo, notificationRepo
    );

    @Test
    void applyCreatesNotificationForPracticeCreator() {
        PracticePost post = post(10L, 100L);
        when(postRepo.findById(10L)).thenReturn(Optional.of(post));
        when(joinRepo.findByPracticePostIdAndApplicantUserId(10L, 200L)).thenReturn(Optional.empty());
        when(joinRepo.save(any(PracticeJoinRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.apply(200L, 10L, new JoinPracticeRequest("一起练"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo(100L);
        assertThat(n.getNoticeType()).isEqualTo("practice_join_applied");
        assertThat(n.getCategory()).isEqualTo("practice");
        assertThat(n.getTargetType()).isEqualTo("practice_post");
        assertThat(n.getTargetId()).isEqualTo(10L);
        assertThat(n.getIsRead()).isFalse();
        assertThat(n.getSentAt()).isNotNull();
    }

    @Test
    void acceptCreatesNotificationForApplicant() {
        PracticePost post = post(10L, 100L);
        PracticeJoinRequest request = joinRequest(20L, 10L, 200L, "pending");
        when(joinRepo.findById(20L)).thenReturn(Optional.of(request));
        when(postRepo.findById(10L)).thenReturn(Optional.of(post));
        when(joinRepo.save(any(PracticeJoinRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepo.save(any(PracticePost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.accept(100L, 20L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo(200L);
        assertThat(n.getNoticeType()).isEqualTo("practice_join_accepted");
        assertThat(n.getCategory()).isEqualTo("practice");
        assertThat(n.getTargetType()).isEqualTo("practice_post");
        assertThat(n.getTargetId()).isEqualTo(10L);
    }

    private PracticePost post(Long id, Long creatorId) {
        PracticePost post = new PracticePost();
        ReflectionTestUtils.setField(post, "id", id);
        post.setCreatorUserId(creatorId);
        post.setDanceStyleId(1L);
        post.setCityId(1L);
        post.setLocationName("练舞房");
        post.setExpectedPeopleMin(2);
        post.setExpectedPeopleMax(4);
        post.setCurrentPeopleCount(1);
        post.setStartAt(OffsetDateTime.now().plusDays(1));
        post.setEndAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        post.setExpiresAt(post.getStartAt());
        post.setPostStatus("published");
        return post;
    }

    private PracticeJoinRequest joinRequest(Long id, Long postId, Long applicantId, String status) {
        PracticeJoinRequest request = new PracticeJoinRequest();
        ReflectionTestUtils.setField(request, "id", id);
        request.setPracticePostId(postId);
        request.setApplicantUserId(applicantId);
        request.setJoinStatus(status);
        return request;
    }
}
