package com.bitdance.community.dto;

import java.time.OffsetDateTime;

public record CommentDto(
    Long id,
    Long contentPostId,
    Long userId,
    Long parentCommentId,
    Long replyToUserId,
    String commentText,
    String commentStatus,
    OffsetDateTime createdAt
) {}
