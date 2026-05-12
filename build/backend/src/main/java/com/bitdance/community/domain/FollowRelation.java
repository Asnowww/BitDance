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
@Table(name = "follow_relation")
@IdClass(FollowRelation.PK.class)
public class FollowRelation {

    @Id
    @Column(name = "follower_user_id")
    private Long followerUserId;

    @Id
    @Column(name = "followee_user_id")
    private Long followeeUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getFollowerUserId() { return followerUserId; }
    public void setFollowerUserId(Long v) { this.followerUserId = v; }
    public Long getFolloweeUserId() { return followeeUserId; }
    public void setFolloweeUserId(Long v) { this.followeeUserId = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static class PK implements Serializable {
        private Long followerUserId;
        private Long followeeUserId;
        public PK() {}
        public PK(Long followerUserId, Long followeeUserId) {
            this.followerUserId = followerUserId; this.followeeUserId = followeeUserId;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(followerUserId, pk.followerUserId)
                && Objects.equals(followeeUserId, pk.followeeUserId);
        }
        @Override public int hashCode() { return Objects.hash(followerUserId, followeeUserId); }
    }
}
