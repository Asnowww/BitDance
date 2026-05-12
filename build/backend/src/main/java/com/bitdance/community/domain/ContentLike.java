package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "content_like")
@IdClass(ContentLike.PK.class)
public class ContentLike {

    @Id
    @Column(name = "content_post_id")
    private Long contentPostId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getContentPostId() { return contentPostId; }
    public void setContentPostId(Long v) { this.contentPostId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static class PK implements Serializable {
        private Long contentPostId;
        private Long userId;
        public PK() {}
        public PK(Long contentPostId, Long userId) {
            this.contentPostId = contentPostId; this.userId = userId;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(contentPostId, pk.contentPostId) && Objects.equals(userId, pk.userId);
        }
        @Override public int hashCode() { return Objects.hash(contentPostId, userId); }
    }
}
