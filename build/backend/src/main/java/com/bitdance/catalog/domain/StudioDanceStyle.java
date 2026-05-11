package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "studio_dance_style")
@IdClass(StudioDanceStyle.PK.class)
public class StudioDanceStyle {

    @Id
    @Column(name = "studio_id")
    private Long studioId;

    @Id
    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = Boolean.FALSE;

    public Long getStudioId() { return studioId; }
    public void setStudioId(Long v) { this.studioId = v; }
    public Long getDanceStyleId() { return danceStyleId; }
    public void setDanceStyleId(Long v) { this.danceStyleId = v; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean v) { this.isPrimary = v; }

    public static class PK implements Serializable {
        private Long studioId;
        private Long danceStyleId;

        public PK() {}
        public PK(Long studioId, Long danceStyleId) {
            this.studioId = studioId; this.danceStyleId = danceStyleId;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(studioId, pk.studioId) && Objects.equals(danceStyleId, pk.danceStyleId);
        }
        @Override public int hashCode() { return Objects.hash(studioId, danceStyleId); }
    }
}
