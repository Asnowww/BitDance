package com.bitdance.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "content_post_topic")
@IdClass(ContentPostTopic.PK.class)
public class ContentPostTopic {

    @Id
    @Column(name = "content_post_id")
    private Long contentPostId;

    @Id
    @Column(name = "topic_tag_id")
    private Long topicTagId;

    public Long getContentPostId() { return contentPostId; }
    public void setContentPostId(Long v) { this.contentPostId = v; }
    public Long getTopicTagId() { return topicTagId; }
    public void setTopicTagId(Long v) { this.topicTagId = v; }

    public static class PK implements Serializable {
        private Long contentPostId;
        private Long topicTagId;
        public PK() {}
        public PK(Long contentPostId, Long topicTagId) {
            this.contentPostId = contentPostId; this.topicTagId = topicTagId;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(contentPostId, pk.contentPostId) && Objects.equals(topicTagId, pk.topicTagId);
        }
        @Override public int hashCode() { return Objects.hash(contentPostId, topicTagId); }
    }
}
