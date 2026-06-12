package com.bitdance.review;

import com.bitdance.message.domain.Notification;
import com.bitdance.message.repository.NotificationRepository;
import com.bitdance.review.domain.Review;
import com.bitdance.review.domain.ReviewReply;
import com.bitdance.review.dto.CreateReplyRequest;
import com.bitdance.review.repository.ReviewReplyRepository;
import com.bitdance.review.repository.ReviewRepository;
import com.bitdance.review.service.ReviewReplyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewReplyServiceNotificationTest {

    private final ReviewReplyRepository replyRepo = mock(ReviewReplyRepository.class);
    private final ReviewRepository reviewRepo = mock(ReviewRepository.class);
    private final NotificationRepository notificationRepo = mock(NotificationRepository.class);

    private final ReviewReplyService service = new ReviewReplyService(replyRepo, reviewRepo, notificationRepo);

    @Test
    void createReplyNotifiesReviewAuthor() {
        Review review = review(7L, 100L);
        when(reviewRepo.findById(7L)).thenReturn(Optional.of(review));
        when(replyRepo.save(any(ReviewReply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(200L, new CreateReplyRequest(7L, "谢谢反馈", true));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo(100L);
        assertThat(n.getNoticeType()).isEqualTo("review_replied");
        assertThat(n.getCategory()).isEqualTo("review");
        assertThat(n.getTargetType()).isEqualTo("review");
        assertThat(n.getTargetId()).isEqualTo(7L);
        assertThat(n.getSentAt()).isNotNull();
    }

    @Test
    void createReplyDoesNotNotifyWhenAuthorRepliesToSelf() {
        Review review = review(7L, 100L);
        when(reviewRepo.findById(7L)).thenReturn(Optional.of(review));
        when(replyRepo.save(any(ReviewReply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(100L, new CreateReplyRequest(7L, "补充说明", false));

        verify(notificationRepo, never()).save(any(Notification.class));
    }

    private Review review(Long id, Long authorId) {
        Review review = new Review();
        ReflectionTestUtils.setField(review, "id", id);
        review.setUserId(authorId);
        review.setTargetType("studio");
        review.setTargetId(1L);
        review.setOverallScore(BigDecimal.valueOf(4.5));
        review.setReviewStatus("published");
        review.setPublishedAt(OffsetDateTime.now());
        return review;
    }
}
